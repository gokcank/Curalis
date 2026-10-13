package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Test

class DueDosesTest {

    private val hour = 3_600_000L
    private val startOfToday = 100 * 24 * hour
    private val now = startOfToday + 19 * hour

    private fun dose(
        id: String,
        time: Long,
        state: ReminderState = ReminderState.DELIVERED,
        medicationId: String = "m1",
        rescheduledTo: Long? = null
    ) = Reminder(id = id, medicationId = medicationId, timeInMillis = time, state = state, rescheduledToMillis = rescheduledTo)

    private fun due(vararg reminders: Reminder, active: Set<String> = setOf("m1", "m2")) =
        DueDoses.unansweredToday(reminders.toList(), active, now, startOfToday).map { it.id }

    @Test
    fun `lists every dose due now and earlier today that is still unanswered, newest first`() {
        val result = due(
            dose("evening-b", now, medicationId = "m2"),
            dose("morning", startOfToday + 8 * hour, ReminderState.SCHEDULED),
            dose("evening-a", now)
        )
        assertEquals(listOf("evening-b", "evening-a", "morning"), result)
    }

    @Test
    fun `answered, snoozed, future and earlier-day doses are left out`() {
        val result = due(
            dose("taken", now, ReminderState.TAKEN),
            dose("skipped", now, ReminderState.SKIPPED),
            dose("snoozed", now, ReminderState.SNOOZED),
            dose("later", now + 2 * hour, ReminderState.SCHEDULED),
            dose("yesterday", startOfToday - 2 * hour)
        )
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `rescheduled doses count at their new time`() {
        val result = due(
            dose("moved-later", startOfToday + 18 * hour, ReminderState.SCHEDULED, rescheduledTo = now + 3 * hour),
            dose("moved-to-now", startOfToday + 8 * hour, ReminderState.SCHEDULED, rescheduledTo = now),
            dose("from-yesterday", startOfToday - 4 * hour, ReminderState.SCHEDULED, rescheduledTo = now - hour)
        )
        assertEquals(listOf("moved-to-now", "from-yesterday"), result)
    }

    @Test
    fun `doses of inactive medications are left out`() {
        assertEquals(emptyList<String>(), due(dose("x", now, medicationId = "archived"), active = setOf("m1")))
    }
}
