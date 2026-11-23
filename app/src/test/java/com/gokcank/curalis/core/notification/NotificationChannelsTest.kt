package com.gokcank.curalis.core.notification

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * Bildirim gönderilen her kanalın sistemde kayıtlı olması gerekir: Android, kayıtlı olmayan
 * bir kanala gönderilen bildirimi hata vermeden düşürür (sabah özeti, ölçüm hatırlatması ve
 * saat dilimi bildirimi bu yüzden görünmüyordu).
 */
@RunWith(RobolectricTestRunner::class)
class NotificationChannelsTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `every channel used for posting is registered`() {
        NotificationHelper(context, NotificationPreferences(context))
        val manager = context.getSystemService(NotificationManager::class.java)

        listOf(
            NotificationHelper.CHANNEL_ID,
            NotificationHelper.CHANNEL_ID_REMINDERS_SILENT,
            NotificationHelper.CHANNEL_ID_REFILL,
            NotificationHelper.CHANNEL_ID_PROACTIVE,
            NotificationHelper.CHANNEL_ID_EVENING_SUMMARY
        ).forEach { id ->
            assertNotNull("channel not registered: $id", manager.getNotificationChannel(id))
        }
    }

    private val manager: NotificationManager get() = context.getSystemService(NotificationManager::class.java)

    private fun reminderChannelIds() = manager.notificationChannels
        .map { it.id }
        .filter { it.startsWith(NotificationHelper.CHANNEL_ID) }

    @Test
    fun `without a chosen sound the original channel keeps the phone's default sound`() {
        val helper = NotificationHelper(context, NotificationPreferences(context))

        assertEquals(NotificationHelper.CHANNEL_ID, helper.reminderChannelId)
        assertEquals(listOf(NotificationHelper.CHANNEL_ID), reminderChannelIds())
        assertEquals(
            Settings.System.DEFAULT_NOTIFICATION_URI,
            manager.getNotificationChannel(NotificationHelper.CHANNEL_ID).sound
        )
        assertNull(helper.reminderSoundTitle())
    }

    @Test
    fun `changing the sound replaces the channel instead of piling up new ones`() {
        val prefs = NotificationPreferences(context)
        val helper = NotificationHelper(context, prefs)
        val chime = Uri.parse("content://media/internal/audio/media/42")

        helper.changeReminderSound(chime)
        assertEquals(listOf("medication_reminders_channel_v1"), reminderChannelIds())
        assertEquals(chime, manager.getNotificationChannel("medication_reminders_channel_v1").sound)
        assertEquals(chime.toString(), prefs.reminderSoundUri)

        // Back to the default: one more channel version, still only one reminder channel.
        helper.changeReminderSound(null)
        assertEquals(listOf("medication_reminders_channel_v2"), reminderChannelIds())
        assertEquals(
            Settings.System.DEFAULT_NOTIFICATION_URI,
            manager.getNotificationChannel("medication_reminders_channel_v2").sound
        )
        assertNull(prefs.reminderSoundUri)

        // The quiet-hours channel is untouched.
        assertNotNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_ID_REMINDERS_SILENT))
        assertNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_ID_REMINDERS_SILENT).sound)
    }

    @Test
    fun `reminders go to the current channel, also after a restart`() {
        val prefs = NotificationPreferences(context)
        NotificationHelper(context, prefs).changeReminderSound(Uri.parse("content://media/internal/audio/media/7"))

        // A new process builds the helper again; it must not bring the old channel back.
        val restarted = NotificationHelper(context, prefs)
        assertEquals(listOf("medication_reminders_channel_v1"), reminderChannelIds())

        restarted.showReminderNotification("r1", "Medicine", "m1")
        assertEquals("medication_reminders_channel_v1", shadowOf(manager).allNotifications.single().channelId)
    }

    @Test
    fun `quiet hours stay silent with a chosen sound`() {
        val prefs = NotificationPreferences(context).apply {
            quietHoursEnabled = true
            // The whole day, so the test does not depend on the clock.
            quietHoursStartMinutes = 0
            quietHoursEndMinutes = 24 * 60
        }
        val helper = NotificationHelper(context, prefs)
        helper.changeReminderSound(Uri.parse("content://media/internal/audio/media/9"))

        helper.showReminderNotification("r1", "Medicine", "m1")
        val posted = shadowOf(manager).allNotifications.single()
        assertEquals(NotificationHelper.CHANNEL_ID_REMINDERS_SILENT, posted.channelId)
        assertNull(manager.getNotificationChannel(posted.channelId).sound)
    }
}
