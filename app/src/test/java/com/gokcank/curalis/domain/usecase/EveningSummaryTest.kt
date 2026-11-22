package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class EveningSummaryTest {

    private val hour = 3_600_000L
    private val startOfToday = 100 * 24 * hour
    private val now = startOfToday + 21 * hour

    private val medications = listOf(
        Medication(id = "m1", name = "A"),
        Medication(id = "archived", name = "B", isArchived = true),
        Medication(id = "suspended", name = "C", isSuspended = true),
        Medication(id = "prn", name = "D", frequencyType = FrequencyType.AS_NEEDED)
    )

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

    private fun unmarked(vararg reminders: Reminder) =
        EveningSummary.unmarkedDoses(reminders.toList(), medications, now, startOfToday).map { it.id }

    @Test
    fun `missed, unanswered and never-delivered doses of today are counted`() {
        val result = unmarked(
            dose("never-rang", startOfToday + 8 * hour, ReminderState.SCHEDULED),
            dose("unanswered", startOfToday + 13 * hour, ReminderState.DELIVERED),
            dose("missed", startOfToday + 18 * hour, ReminderState.MISSED),
            dose("at-summary-time", now, ReminderState.DELIVERED)
        )
        assertEquals(listOf("never-rang", "unanswered", "missed", "at-summary-time"), result)
    }

    @Test
    fun `marked doses are not counted`() {
        val result = unmarked(
            dose("taken", startOfToday + 8 * hour, ReminderState.TAKEN),
            dose("skipped", startOfToday + 9 * hour, ReminderState.SKIPPED),
            dose("snoozed", startOfToday + 10 * hour, ReminderState.SNOOZED),
            dose("cancelled", startOfToday + 11 * hour, ReminderState.CANCELLED)
        )
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `doses after the summary time and doses of other days are not counted`() {
        val result = unmarked(
            dose("later-tonight", now + hour),
            dose("yesterday", startOfToday - hour),
            dose("moved-to-later", startOfToday + 20 * hour, rescheduledTo = now + hour),
            dose("moved-earlier", now + hour, rescheduledTo = startOfToday + 20 * hour)
        )
        assertEquals(listOf("moved-earlier"), result)
    }

    @Test
    fun `placebo doses and doses of inactive or as-needed medications are not counted`() {
        val result = unmarked(
            dose("placebo", startOfToday + 8 * hour, isPlacebo = true),
            dose("archived", startOfToday + 8 * hour, medicationId = "archived"),
            dose("suspended", startOfToday + 8 * hour, medicationId = "suspended"),
            dose("prn", startOfToday + 8 * hour, medicationId = "prn"),
            dose("deleted", startOfToday + 8 * hour, medicationId = "unknown"),
            dose("real", startOfToday + 8 * hour)
        )
        assertEquals(listOf("real"), result)
    }

    @Test
    fun `start of day is local midnight`() {
        val noon = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 23, 12, 34, 56)
        }.timeInMillis
        val start = Calendar.getInstance().apply { timeInMillis = EveningSummary.startOfDay(noon) }
        assertEquals(23, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, start.get(Calendar.MINUTE))
        assertEquals(0, start.get(Calendar.SECOND))
        assertEquals(0, start.get(Calendar.MILLISECOND))
    }
}
