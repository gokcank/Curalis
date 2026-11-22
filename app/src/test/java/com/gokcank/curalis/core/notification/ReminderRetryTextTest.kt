package com.gokcank.curalis.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.util.Calendar

/** İlk bildirim, tekrar ve son tekrar birbirinden ayırt edilebilir metinler taşır. */
@RunWith(RobolectricTestRunner::class)
class ReminderRetryTextTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val doseTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 30)
    }.timeInMillis

    private fun post(
        id: String,
        level: LockScreenPrivacy = LockScreenPrivacy.FULL,
        isRetry: Boolean = false,
        isLastAttempt: Boolean = false
    ): Notification {
        val prefs = NotificationPreferences(context).apply { lockScreenPrivacy = level }
        NotificationHelper(context, prefs).showReminderNotification(
            reminderId = id,
            medicationName = "Aspirin",
            medicationId = "m1",
            isLastAttempt = isLastAttempt,
            isRetry = isRetry,
            doseTimeMillis = doseTime
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        return shadowOf(manager).getNotification(id.hashCode())
    }

    private fun Notification.title() = extras.getCharSequence(Notification.EXTRA_TITLE).toString()
    private fun Notification.text() = extras.getCharSequence(Notification.EXTRA_TEXT).toString()

    @Test
    fun `first reminder keeps its usual text`() {
        val n = post("first")
        assertEquals("Medication Time", n.title())
        assertEquals("Medication: Aspirin", n.text())
    }

    @Test
    fun `a retry says the dose is not marked yet`() {
        val n = post("retry", isRetry = true)
        assertEquals("Not marked yet", n.title())
        assertEquals("Medication: Aspirin", n.text())
    }

    @Test
    fun `the last retry warns that the dose will count as missed`() {
        val n = post("last", isRetry = true, isLastAttempt = true)
        assertEquals("Last reminder", n.title())
        assertEquals("Medication: Aspirin · If not marked, it will count as missed.", n.text())
        assertEquals("Last reminder", n.publicVersion.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
    }

    @Test
    fun `limited privacy keeps the name out of retries too`() {
        val retry = post("limited-retry", LockScreenPrivacy.LIMITED, isRetry = true)
        assertEquals("Not marked yet", retry.title())
        assertEquals("At 08:30", retry.text())

        val last = post("limited-last", LockScreenPrivacy.LIMITED, isRetry = true, isLastAttempt = true)
        assertEquals("At 08:30 · If not marked, it will count as missed.", last.text())
        assertFalse(last.extras.toString().contains("Aspirin"))
    }
}
