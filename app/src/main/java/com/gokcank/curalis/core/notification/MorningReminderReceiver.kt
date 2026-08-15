package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Günlük "ilaçlarını yanına al" hatırlatmasını gösterir ve bir sonraki günü yeniden kurar
 * (bkz. AlarmScheduler.scheduleMorningReminder — tek seferlik alarm, kendini yeniler).
 */
@AndroidEntryPoint
class MorningReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationPreferences: NotificationPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (notificationPreferences.morningReminderEnabled) {
            notificationHelper.showMorningReminderNotification()
        }
        // Ayar bu tetiklenme sırasında kapatılmış olsa bile scheduleMorningReminder()
        // bunu kendi içinde kontrol edip alarmı iptal eder.
        alarmScheduler.scheduleMorningReminder()
    }
}
