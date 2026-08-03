package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var acknowledgeReminderUseCase: AcknowledgeReminderUseCase

    @Inject
    lateinit var scheduleReminderUseCase: ScheduleReminderUseCase

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderId = intent?.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return
        val action = intent.action ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationHelper.dismissNotification(reminderId)

                when (action) {
                    NotificationHelper.ACTION_TAKEN -> {
                        acknowledgeReminderUseCase(reminderId, ReminderState.TAKEN)
                    }
                    NotificationHelper.ACTION_SKIP -> {
                        acknowledgeReminderUseCase(reminderId, ReminderState.SKIPPED)
                    }
                    NotificationHelper.ACTION_SNOOZE -> {
                        acknowledgeReminderUseCase(reminderId, ReminderState.SNOOZED)
                        
                        // Schedule a new alarm for 10 minutes later
                        val medicationName = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_NAME) ?: "Medication"
                        val medicationId = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_ID) ?: ""
                        val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
                        val newReminder = Reminder(
                            medicationId = medicationId,
                            timeInMillis = snoozeTime,
                            state = ReminderState.SCHEDULED
                        )
                        scheduleReminderUseCase(newReminder)
                        alarmScheduler.schedule(newReminder, medicationName)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
