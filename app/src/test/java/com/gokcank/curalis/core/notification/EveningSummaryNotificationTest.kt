package com.gokcank.curalis.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.gokcank.curalis.core.navigation.OpenDestination
import com.gokcank.curalis.core.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
class EveningSummaryNotificationTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    private fun at(day: Int, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun `the notification states the count without medication names and opens the timeline`() {
        val preferences = NotificationPreferences(context)
        NotificationHelper(context, preferences).showEveningSummaryNotification(2)

        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = shadowOf(manager).allNotifications.single()
        assertEquals(
            "2 doses are not marked today",
            notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
        )
        assertEquals(NotificationHelper.CHANNEL_ID_EVENING_SUMMARY, notification.channelId)
        assertEquals(Notification.VISIBILITY_PUBLIC, notification.visibility)
        val opened = shadowOf(notification.contentIntent).savedIntent
        assertEquals(Screen.DailyTimeline.route, OpenDestination.routeFor(opened))
    }

    @Test
    fun `the hidden lock-screen level keeps the summary off the lock screen`() {
        val preferences = NotificationPreferences(context).apply { lockScreenPrivacy = LockScreenPrivacy.HIDDEN }
        NotificationHelper(context, preferences).showEveningSummaryNotification(1)

        val manager = context.getSystemService(NotificationManager::class.java)
        assertEquals(Notification.VISIBILITY_SECRET, shadowOf(manager).allNotifications.single().visibility)
    }

    @Test
    fun `the summary is off by default at 21_00`() {
        val preferences = NotificationPreferences(context)
        assertFalse(preferences.eveningSummaryEnabled)
        assertEquals(21 * 60, preferences.eveningSummaryMinutes)
    }

    @Test
    fun `the next trigger is today before the chosen time and tomorrow from then on`() {
        val minutes = 21 * 60
        assertEquals(at(23, 21, 0), AlarmScheduler.nextDailyTriggerMillis(at(23, 20, 59), minutes))
        assertEquals(at(24, 21, 0), AlarmScheduler.nextDailyTriggerMillis(at(23, 21, 0), minutes))
        assertEquals(at(24, 21, 0), AlarmScheduler.nextDailyTriggerMillis(at(23, 23, 30), minutes))
    }
}
