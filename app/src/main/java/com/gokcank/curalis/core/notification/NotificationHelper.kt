package com.gokcank.curalis.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gokcank.curalis.R
import com.gokcank.curalis.presentation.alarm.AlarmFullScreenActivity
import com.gokcank.curalis.presentation.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Enjeksiyon anında zaten [init] içinde kanallar oluşturulur; bu fonksiyon yalnızca
     * çağıran tarafta (uygulama açılışında) niyeti açık şekilde ifade etmek için var.
     */
    fun ensureNotificationChannelsCreated() {}

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
            }

            // Sessiz saatlerde kullanılan, sesi/titreşimi olmayan düşük öncelikli kanal.
            val silentReminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS_SILENT,
                context.getString(R.string.notification_channel_name_silent),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_desc_silent)
                enableVibration(false)
                setSound(null, null)
            }

            // Stok/yeniden reçete uyarıları; kullanıcı doz hatırlatmalarını açık tutup
            // bunu ayrı ayrı sistem ayarlarından kısabilir.
            val refillChannel = NotificationChannel(
                CHANNEL_ID_REFILL,
                context.getString(R.string.notification_channel_name_refill),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc_refill)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(silentReminderChannel)
            notificationManager.createNotificationChannel(refillChannel)
        }
    }

    fun showReminderNotification(reminderId: String, medicationName: String, medicationId: String = "") {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // FullScreen LockScreen Activity Intent
        val fullScreenIntent = Intent(context, AlarmFullScreenActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmScheduler.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(AlarmScheduler.EXTRA_MEDICATION_ID, medicationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            (reminderId + "_fullscreen").hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_taken").hashCode(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze (10 mins)
        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_SNOOZE_MINUTES, 10)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SKIP
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_skip").hashCode(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isQuietHours = notificationPreferences.isWithinQuietHours()
        val reminderChannelId = if (isQuietHours) CHANNEL_ID_REMINDERS_SILENT else CHANNEL_ID
        val hideOnLockScreen = notificationPreferences.hideMedicationNameOnLockScreen

        val publicVersion = NotificationCompat.Builder(context, reminderChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title))
            .build()

        val builder = NotificationCompat.Builder(context, reminderChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text_medication, medicationName))
            .setPriority(if (isQuietHours) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(if (hideOnLockScreen) NotificationCompat.VISIBILITY_PRIVATE else NotificationCompat.VISIBILITY_PUBLIC)
            .setPublicVersion(publicVersion)
            .addAction(
                android.R.drawable.ic_menu_save,
                context.getString(R.string.notification_action_take),
                takenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_popup_sync,
                context.getString(R.string.notification_action_snooze),
                snoozePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.notification_action_skip),
                skipPendingIntent
            )

        // Sessiz saatlerde kullanıcıyı uyandırmamak için tam ekran uyarı gösterilmiyor.
        if (!isQuietHours) {
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        notificationManager.notify(reminderId.hashCode(), builder.build())
    }

    fun showRefillWarningNotification(medicationName: String, remainingStock: Int) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medicationName.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REFILL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Stok azalıyor")
            .setContentText("$medicationName ilacınız bitmek üzere! Kalan: $remainingStock doz. Reçetenizi yenilemeyi unutmayın.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify((medicationName + "_refill").hashCode(), builder.build())
    }

    fun dismissNotification(reminderId: String) {
        notificationManager.cancel(reminderId.hashCode())
    }

    fun showGroupedReminderNotification(
        reminderIds: List<String>,
        medicationNames: List<String>,
        medicationIds: List<String>
    ) {
        if (reminderIds.isEmpty() || medicationNames.isEmpty()) return

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderIds.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val joinedNames = medicationNames.joinToString(", ")
        val groupTitle = "${medicationNames.size} ilacın saati geldi"
        val groupText = "Alınacak İlaçlar: $joinedNames"

        val takeAllIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_TAKE_ALL
            putExtra(EXTRA_REMINDER_IDS, reminderIds.toTypedArray())
            putExtra(EXTRA_MEDICATION_IDS, medicationIds.toTypedArray())
        }
        val takeAllPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderIds.hashCode(),
            takeAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isQuietHours = notificationPreferences.isWithinQuietHours()
        val groupChannelId = if (isQuietHours) CHANNEL_ID_REMINDERS_SILENT else CHANNEL_ID
        val hideOnLockScreen = notificationPreferences.hideMedicationNameOnLockScreen

        val publicVersion = NotificationCompat.Builder(context, groupChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title))
            .build()

        val builder = NotificationCompat.Builder(context, groupChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(groupTitle)
            .setContentText(groupText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(groupText))
            .setPriority(if (isQuietHours) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(if (hideOnLockScreen) NotificationCompat.VISIBILITY_PRIVATE else NotificationCompat.VISIBILITY_PUBLIC)
            .setPublicVersion(publicVersion)
            .addAction(
                android.R.drawable.ic_menu_save,
                "Hepsini Aldım",
                takeAllPendingIntent
            )

        notificationManager.notify(reminderIds.hashCode(), builder.build())
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders_channel"
        const val CHANNEL_ID_REMINDERS_SILENT = "medication_reminders_silent_channel"
        const val CHANNEL_ID_REFILL = "refill_warnings_channel"
        const val ACTION_TAKEN = "com.gokcank.curalis.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.gokcank.curalis.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.gokcank.curalis.ACTION_SKIP"
        const val ACTION_TAKE_ALL = "com.gokcank.curalis.ACTION_TAKE_ALL"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_IDS = "extra_reminder_ids"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_IDS = "extra_medication_ids"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_SKIP_REASON = "extra_skip_reason"
    }
}
