package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

@AndroidEntryPoint
class MissedDoseCheckReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase

    @Inject
    lateinit var acknowledgeReminderUseCase: AcknowledgeReminderUseCase

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: return
        val medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = getRemindersForMedicationUseCase(medicationId).firstOrNull() ?: emptyList()
                val targetReminder = reminders.find { it.id == reminderId }

                if (targetReminder == null) {
                    // ReminderReceiver hiç çalışmadıysa (ör. OEM pil optimizasyonu alarmı
                    // öldürdüyse) bu kayıt hiç oluşmamış olur. Önceden bu durumda hiçbir şey
                    // kaydedilmiyordu; artık "kaçırıldı" olarak açıkça işaretleniyor, ki bu iki
                    // farklı arıza türünü (hiç çalmadı / çaldı ama yanıtlanmadı) birbirinden
                    // ayırt edebilelim.
                    reminderRepository.insertReminder(
                        Reminder(
                            id = reminderId,
                            medicationId = medicationId,
                            timeInMillis = System.currentTimeMillis(),
                            state = ReminderState.MISSED
                        )
                    )
                } else if (targetReminder.state == ReminderState.SCHEDULED || targetReminder.state == ReminderState.DELIVERED) {
                    acknowledgeReminderUseCase(reminderId, ReminderState.MISSED)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
