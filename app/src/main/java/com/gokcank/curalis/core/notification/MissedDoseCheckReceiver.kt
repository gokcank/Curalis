package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
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
 * bildirimi fark etmezse doz sessizce kaçmış sayılırdı. Şimdi bildirim, kullanıcının seçtiği
 * aralıkla ve sayıda yeniden gösteriliyor; son bildirimden bir aralık sonra hâlâ yanıt yoksa
 * "kaçırıldı" olarak işaretleniyor (bkz. [ReminderRetryPolicy]).
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

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var notificationPreferences: NotificationPreferences

    @Inject
    lateinit var voiceReminderSpeaker: VoiceReminderSpeaker

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: return
        val medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: "İlaç"
        // Eski sürümden kalan, deneme sayısı taşımayan bir kontrol dozu doğrudan kaçırıldı sayar.
        val attempt = intent.getIntExtra(AlarmScheduler.EXTRA_MISSED_CHECK_ATTEMPT, Int.MAX_VALUE)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Silinen ilaçların alarmları iptal edilse de bu tekrar kontrolü ayrı bir
                // alarm olarak kalabiliyor; artık geçerli olmayan bir ilaç için ne bildirim
                // gösterilir ne de sahipsiz bir "kaçırıldı" kaydı açılır.
                val medication = medicationRepository.getMedicationById(medicationId).firstOrNull()
                if (medication == null || medication.isArchived || medication.isSuspended) return@launch

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
                    // Ayar her kontrolde yeniden okunur; zincirin ortasında azaltılırsa
                    // sınırı aşmış bir kontrol dozu hemen kaçırıldı sayar.
                    when (val step = ReminderRetryPolicy.stepFor(attempt, notificationPreferences.reminderAttempts)) {
                        is ReminderRetryPolicy.Step.Repeat -> {
                            // Kullanıcı hâlâ yanıt vermedi; bildirimi yeniden göster ve bir sonraki
                            // denemeyi kur. Son bildirim (doz "kaçırıldı" olarak işaretlenmeden
                            // önceki son fırsat) popup tercihinden bağımsız olarak zorla gösterilir.
                            voiceReminderSpeaker.announceDose(medicationName)
                            notificationHelper.showReminderNotification(
                                reminderId,
                                medicationName,
                                medicationId,
                                isLastAttempt = step.isLast,
                                isRetry = true,
                                doseTimeMillis = targetReminder.rescheduledToMillis ?: targetReminder.timeInMillis
                            )
                            alarmScheduler.scheduleMissedDoseCheck(
                                reminderId = reminderId,
                                medicationId = medicationId,
                                medicationName = medicationName,
                                delayMinutes = notificationPreferences.reminderRetryIntervalMinutes,
                                attempt = step.nextAttempt
                            )
                        }
                        ReminderRetryPolicy.Step.MarkMissed ->
                            acknowledgeReminderUseCase(reminderId, ReminderState.MISSED)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kaçırılan doz kontrolü sırasında hata oluştu", e)
            } finally {
                voiceReminderSpeaker.awaitSilence()
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "MissedDoseCheckReceiver"
    }
}
