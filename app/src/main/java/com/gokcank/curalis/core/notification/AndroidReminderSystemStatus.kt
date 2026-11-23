package com.gokcank.curalis.core.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.gokcank.curalis.domain.repository.ReminderSystemStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** İzin durumlarını Android'den okur (bkz. [ReminderPermissions]). */
@Singleton
class AndroidReminderSystemStatus @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences
) : ReminderSystemStatus {

    /**
     * Uygulamanın bildirimleri kapalıysa ya da kullanıcı yalnızca "İlaç hatırlatmaları"
     * kategorisini kapattıysa hatırlatma görünmez. Sessiz saatler kanalı ayrı tutulur;
     * yalnızca sessiz saatlerde kullanıldığı için burada denetlenmez.
     */
    override fun areReminderNotificationsEnabled(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val channel = context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(NotificationHelper.reminderChannelIdFor(notificationPreferences.reminderChannelVersion))
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    override fun canScheduleExactAlarms(): Boolean = ReminderPermissions.canScheduleExactAlarms(context)

    override fun isIgnoringBatteryOptimizations(): Boolean =
        ReminderPermissions.isIgnoringBatteryOptimizations(context)
}
