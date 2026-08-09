package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderId = intent?.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: return
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: "Medication"
        val medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: ""
        val timeInMillis = intent.getLongExtra(AlarmScheduler.EXTRA_TIME_MILLIS, System.currentTimeMillis())
        val kind = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_KIND) ?: AlarmScheduler.KIND_MEDICATION

        notificationHelper.showReminderNotification(reminderId, medicationName, medicationId)

        // Randevu hatırlatıcıları Reminder tablosunda doz takibi yapmaz; yalnızca
        // ilaç hatırlatıcıları için "iletildi" durumu kaydedilir ve kaçırılma kontrolü kurulur.
        if (kind == AlarmScheduler.KIND_MEDICATION && medicationId.isNotBlank()) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Alarmın gerçekten tetiklendiğini kalıcı olarak işaretle. Bu satır
                    // olmadan "alarm hiç çalmadı" (SCHEDULED kalır) ile "çaldı ama
                    // yanıtlanmadı" ayrımı yapılamıyordu.
                    val existing = reminderRepository.getReminderById(reminderId)
                    if (existing == null) {
                        reminderRepository.insertReminder(
                            Reminder(
                                id = reminderId,
                                medicationId = medicationId,
                                timeInMillis = timeInMillis,
                                state = ReminderState.DELIVERED
                            )
                        )
                    } else if (existing.state == ReminderState.SCHEDULED) {
                        reminderRepository.updateReminder(existing.copy(state = ReminderState.DELIVERED))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Hatırlatıcı 'iletildi' olarak işaretlenirken hata oluştu", e)
                } finally {
                    pendingResult.finish()
                }
            }

            alarmScheduler.scheduleMissedDoseCheck(reminderId, medicationId, 30)
        }
    }

    companion object {
        private const val TAG = "ReminderReceiver"
    }
}
