package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersForMedicationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Bir doz hatırlatıcısı yanıtsız kaldığında devreye giren, kendini birkaç kez tekrarlayan
 * kontrol zinciri. Önceden tek seferlik bir kontroldü: alarm bir kez çalar, 30 dakika sonra
 * hâlâ yanıtlanmamışsa doğrudan "kaçırıldı" işaretlenirdi — kullanıcı o 30 dakika içinde
 * bildirimi fark etmezse doz sessizce kaçmış sayılırdı. Şimdi her [RETRY_INTERVAL_MINUTES]
 * dakikada bir bildirim yeniden gösteriliyor; yalnızca [MAX_ATTEMPTS]. denemeden sonra hâlâ
 * yanıt yoksa "kaçırıldı" olarak işaretleniyor.
 */
@AndroidEntryPoint
class MissedDoseCheckReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase

    @Inject
    lateinit var acknowledgeReminderUseCase: AcknowledgeReminderUseCase

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: return
        val medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: "İlaç"
        val attempt = intent.getIntExtra(AlarmScheduler.EXTRA_MISSED_CHECK_ATTEMPT, MAX_ATTEMPTS)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = getRemindersForMedicationUseCase(medicationId).firstOrNull() ?: emptyList()
                val targetReminder = reminders.find { it.id == reminderId }

                if (targetReminder == null) {
                    // ReminderReceiver hiç çalışmadıysa (ör. OEM pil optimizasyonu alarmı
                    // öldürdüyse) bu kayıt hiç oluşmamış olur. Bu durumda birincil alarm
                    // sisteminin çalıştığına dair bir garanti olmadığı için tekrar denemek
                    // yerine doğrudan "kaçırıldı" olarak işaretlenir.
                    reminderRepository.insertReminder(
                        Reminder(
                            id = reminderId,
                            medicationId = medicationId,
                            timeInMillis = System.currentTimeMillis(),
                            state = ReminderState.MISSED
                        )
                    )
                } else if (targetReminder.state == ReminderState.SCHEDULED || targetReminder.state == ReminderState.DELIVERED) {
                    if (attempt < MAX_ATTEMPTS) {
                        // Kullanıcı hâlâ yanıt vermedi; bildirimi yeniden göster ve bir
                        // sonraki denemeyi kur. attempt + 1 == MAX_ATTEMPTS ise bu, doz
                        // "kaçırıldı" olarak işaretlenmeden önceki son bildirim demektir —
                        // popup tercihinden bağımsız olarak zorla gösterilir.
                        notificationHelper.showReminderNotification(
                            reminderId,
                            medicationName,
                            medicationId,
                            isLastAttempt = attempt + 1 == MAX_ATTEMPTS
                        )
                        alarmScheduler.scheduleMissedDoseCheck(
                            reminderId = reminderId,
                            medicationId = medicationId,
                            medicationName = medicationName,
                            delayMinutes = RETRY_INTERVAL_MINUTES,
                            attempt = attempt + 1
                        )
                    } else {
                        acknowledgeReminderUseCase(reminderId, ReminderState.MISSED)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kaçırılan doz kontrolü sırasında hata oluştu", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "MissedDoseCheckReceiver"

        /** Bildirim, kullanıcı yanıtlamadığı sürece bu kadar dakikada bir tekrarlanır. */
        const val RETRY_INTERVAL_MINUTES = 10

        /** Bu kadar denemeden sonra hâlâ yanıt yoksa doz "kaçırıldı" olarak işaretlenir. */
        const val MAX_ATTEMPTS = 3
    }
}
