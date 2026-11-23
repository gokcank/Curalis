package com.gokcank.curalis.core.notification

import android.content.Context
import com.gokcank.curalis.core.notification.ReminderRetryPolicy.Step
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ReminderRetryPolicyTest {

    /** Walks the chain the way the receivers do: checks start at attempt 1. */
    private fun chain(maxAttempts: Int): List<Step> {
        val steps = mutableListOf<Step>()
        var attempt = 1
        while (true) {
            val step = ReminderRetryPolicy.stepFor(attempt, maxAttempts)
            steps += step
            if (step !is Step.Repeat) return steps
            attempt = step.nextAttempt
        }
    }

    @Test
    fun `defaults keep the old behavior - two repeats, missed at minute 30`() {
        assertEquals(
            listOf(Step.Repeat(isLast = false, nextAttempt = 2), Step.Repeat(isLast = true, nextAttempt = 3), Step.MarkMissed),
            chain(ReminderRetryPolicy.DEFAULT_ATTEMPTS)
        )
        assertEquals(
            30,
            ReminderRetryPolicy.minutesUntilMissed(
                ReminderRetryPolicy.DEFAULT_ATTEMPTS,
                ReminderRetryPolicy.DEFAULT_INTERVAL_MINUTES
            )
        )
    }

    @Test
    fun `one reminder means no repeats - the first check marks the dose missed`() {
        assertEquals(listOf(Step.MarkMissed), chain(1))
        assertEquals(15, ReminderRetryPolicy.minutesUntilMissed(1, 15))
    }

    @Test
    fun `five reminders repeat four times and only the fourth repeat is the last`() {
        val steps = chain(5)
        assertEquals(5, steps.size)
        assertEquals(listOf(false, false, false, true), steps.dropLast(1).map { (it as Step.Repeat).isLast })
        assertEquals(Step.MarkMissed, steps.last())
        assertEquals(150, ReminderRetryPolicy.minutesUntilMissed(5, 30))
    }

    @Test
    fun `lowering the setting mid-chain marks an over-limit check missed`() {
        // Attempt 3 was scheduled under a setting of 5, then the user chose 2.
        assertEquals(Step.MarkMissed, ReminderRetryPolicy.stepFor(3, 2))
        // A check from an old version without an attempt number.
        assertEquals(Step.MarkMissed, ReminderRetryPolicy.stepFor(Int.MAX_VALUE, 5))
    }

    @Test
    fun `preferences default to 3 times every 10 minutes and reject out-of-range values`() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = NotificationPreferences(context)
        assertEquals(3, prefs.reminderAttempts)
        assertEquals(10, prefs.reminderRetryIntervalMinutes)

        prefs.reminderAttempts = 5
        prefs.reminderRetryIntervalMinutes = 30
        assertEquals(5, prefs.reminderAttempts)
        assertEquals(30, prefs.reminderRetryIntervalMinutes)

        prefs.reminderAttempts = 9
        prefs.reminderRetryIntervalMinutes = 7
        assertEquals(5, prefs.reminderAttempts)
        assertEquals(10, prefs.reminderRetryIntervalMinutes)

        // A corrupt stored value is read back within range too.
        context.getSharedPreferences("curalis_notification_prefs", Context.MODE_PRIVATE)
            .edit().putInt("reminder_attempts", 0).commit()
        assertEquals(1, prefs.reminderAttempts)
    }
}
