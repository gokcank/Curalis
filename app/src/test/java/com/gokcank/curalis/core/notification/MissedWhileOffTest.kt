package com.gokcank.curalis.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.gokcank.curalis.core.navigation.OpenDestination
import com.gokcank.curalis.core.navigation.Screen
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class MissedWhileOffTest {

    private val hour = 3_600_000L
    private val now = 1_800_000_000_000L
    private val context: Context get() = RuntimeEnvironment.getApplication()

    private fun dose(
        id: String,
        time: Long,
        state: ReminderState = ReminderState.SCHEDULED,
        rescheduledTo: Long? = null,
        isPlacebo: Boolean = false
    ) = Reminder(
        id = id,
        medicationId = "m1",
        timeInMillis = time,
        state = state,
        rescheduledToMillis = rescheduledTo,
        isPlacebo = isPlacebo
    )

    @Test
    fun `only past doses still scheduled are stale, judged by their rescheduled time`() {
        val stale = staleScheduledDoses(
            listOf(
                dose("past", now - hour),
                dose("future", now + hour),
                dose("delivered", now - hour, ReminderState.DELIVERED),
                dose("taken", now - hour, ReminderState.TAKEN),
                dose("moved-later", now - hour, rescheduledTo = now + hour),
                dose("moved-past", now - 3 * hour, rescheduledTo = now - hour)
            ),
            now
        )
        assertEquals(listOf("past", "moved-past"), stale.map { it.id })
    }

    @Test
    fun `placebo doses are not counted as missed while the phone was off`() {
        val stale = listOf(dose("a", now - hour), dose("placebo", now - hour, isPlacebo = true), dose("b", now - 2 * hour))
        assertEquals(2, countMissedWhileOff(stale))
    }

    @Test
    fun `the notification states the count and opens the timeline`() {
        NotificationHelper(context, NotificationPreferences(context)).showMissedWhileOffNotification(3)

        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = shadowOf(manager).allNotifications.single()
        assertEquals(
            "3 doses were missed while the phone was off",
            notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
        )
        assertEquals(NotificationHelper.CHANNEL_ID_PROACTIVE, notification.channelId)
        val opened = shadowOf(notification.contentIntent).savedIntent
        assertEquals(Screen.DailyTimeline.route, OpenDestination.routeFor(opened))
    }

    @Test
    fun `unknown destinations are ignored`() {
        assertNull(OpenDestination.routeFor(Intent().putExtra(OpenDestination.EXTRA, "settings")))
        assertNull(OpenDestination.routeFor(Intent()))
        assertNull(OpenDestination.routeFor(null))
    }
}
