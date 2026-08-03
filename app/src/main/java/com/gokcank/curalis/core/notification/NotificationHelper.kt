package com.gokcank.curalis.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gokcank.curalis.R
import com.gokcank.curalis.presentation.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(reminderId: String, medicationName: String, medicationId: String) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra(EXTRA_REMINDER_ID, reminderId)
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
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_skip").hashCode(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text_medication, medicationName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, context.getString(R.string.notification_action_take), takenPendingIntent)
            .addAction(0, context.getString(R.string.notification_action_snooze), snoozePendingIntent)
            .addAction(0, context.getString(R.string.notification_action_skip), skipPendingIntent)

        notificationManager.notify(reminderId.hashCode(), builder.build())
    }

    fun dismissNotification(reminderId: String) {
        notificationManager.cancel(reminderId.hashCode())
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders_channel"
        const val ACTION_TAKEN = "com.gokcank.curalis.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.gokcank.curalis.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.gokcank.curalis.ACTION_SKIP"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
    }
}
