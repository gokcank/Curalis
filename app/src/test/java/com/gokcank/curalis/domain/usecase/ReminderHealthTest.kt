package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderHealthTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour
    private val now = 1_800_000_000_000L

    private val medication = Medication(id = "m1", name = "A", startDate = now - 10 * day)

    private fun dose(
        id: String,
        time: Long,
        state: ReminderState = ReminderState.SCHEDULED,
        medicationId: String = "m1",
        rescheduledTo: Long? = null,
        isPlacebo: Boolean = false
    ) = Reminder(
        id = id,
        medicationId = medicationId,
        timeInMillis = time,
        state = state,
        rescheduledToMillis = rescheduledTo,
        isPlacebo = isPlacebo
    )

    private fun undelivered(vararg reminders: Reminder, medications: List<Medication> = listOf(medication)) =
        ReminderHealth.undeliveredDoses(reminders.toList(), medications, now).map { it.id }

    @Test
    fun `a dose still scheduled well past its time within the last day counts as undelivered`() {
        assertEquals(listOf("late"), undelivered(dose("late", now - 2 * hour)))
    }

    @Test
    fun `doses inside the grace period, in the future or older than a day are left out`() {
        val result = undelivered(
            dose("grace", now - 10 * minute),
            dose("future", now + hour),
            dose("old", now - day - minute)
        )
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `doses whose alarm went off or that were answered are left out`() {
        val result = undelivered(
            dose("delivered", now - hour, ReminderState.DELIVERED),
            dose("taken", now - hour, ReminderState.TAKEN),
            dose("skipped", now - hour, ReminderState.SKIPPED),
            dose("missed", now - hour, ReminderState.MISSED),
            dose("snoozed", now - hour, ReminderState.SNOOZED)
        )
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `a rescheduled dose is judged by its new time`() {
        val result = undelivered(
            // Planned two hours ago, moved to the future: not due yet.
            dose("moved-later", now - 2 * hour, rescheduledTo = now + hour),
            // Planned two days ago, moved to an hour ago: due and never went off.
            dose("moved-into-window", now - 2 * day, rescheduledTo = now - hour)
        )
        assertEquals(listOf("moved-into-window"), result)
    }

    @Test
    fun `doses without an alarm are left out`() {
        val result = undelivered(
            dose("placebo", now - hour, isPlacebo = true),
            dose("archived", now - hour, medicationId = "archived"),
            dose("suspended", now - hour, medicationId = "suspended"),
            dose("prn", now - hour, medicationId = "prn"),
            dose("deleted", now - hour, medicationId = "deleted"),
            medications = listOf(
                medication,
                Medication(id = "archived", name = "B", isArchived = true),
                Medication(id = "suspended", name = "C", isSuspended = true),
                Medication(id = "prn", name = "D", frequencyType = FrequencyType.AS_NEEDED)
            )
        )
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `doses after the end of a fixed-length treatment are left out`() {
        val finished = Medication(id = "done", name = "E", startDate = now - 5 * day, treatmentDurationDays = 2)
        assertEquals(emptyList<String>(), undelivered(dose("after-end", now - hour, medicationId = "done"), medications = listOf(finished)))
    }

    @Test
    fun `permission problems are reported only while some medication sets alarms`() {
        val withAlarms = ReminderHealth.report(
            reminders = emptyList(),
            medications = listOf(medication),
            now = now,
            notificationsEnabled = false,
            exactAlarmsAllowed = false,
            batteryOptimizationIgnored = false
        )
        assertTrue(withAlarms.notificationsBlocked)
        assertTrue(withAlarms.exactAlarmsDenied)
        assertTrue(withAlarms.batteryOptimized)
        assertTrue(withAlarms.hasIssues)

        val onlyAsNeeded = ReminderHealth.report(
            reminders = emptyList(),
            medications = listOf(Medication(id = "prn", name = "D", frequencyType = FrequencyType.AS_NEEDED)),
            now = now,
            notificationsEnabled = false,
            exactAlarmsAllowed = false,
            batteryOptimizationIgnored = false
        )
        assertFalse(onlyAsNeeded.hasIssues)
    }

    @Test
    fun `everything in order reports no issues`() {
        val report = ReminderHealth.report(
            reminders = listOf(dose("taken", now - hour, ReminderState.TAKEN)),
            medications = listOf(medication),
            now = now,
            notificationsEnabled = true,
            exactAlarmsAllowed = true,
            batteryOptimizationIgnored = true
        )
        assertEquals(ReminderHealthReport(), report)
        assertFalse(report.hasIssues)
    }
}
