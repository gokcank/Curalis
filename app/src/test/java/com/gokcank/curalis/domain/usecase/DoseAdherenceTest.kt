package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseAdherenceTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val due = 100 * 24 * hour + 8 * hour
    private val threshold = 60

    private fun taken(takenAt: Long?, rescheduledTo: Long? = null, placebo: Boolean = false) = Reminder(
        medicationId = "m1",
        timeInMillis = due,
        state = ReminderState.TAKEN,
        takenAtMillis = takenAt,
        rescheduledToMillis = rescheduledTo,
        isPlacebo = placebo
    )

    private fun dose(state: ReminderState) = Reminder(medicationId = "m1", timeInMillis = due, state = state)

    @Test
    fun `a dose taken more than the threshold after its time is late`() {
        assertFalse(DoseAdherence.isLate(taken(due + 60 * minute), threshold))
        assertTrue(DoseAdherence.isLate(taken(due + 61 * minute), threshold))
        assertFalse(DoseAdherence.isLate(taken(due - 2 * hour), threshold))
    }

    @Test
    fun `lateness of a rescheduled dose is measured from its new time`() {
        val rescheduledTo = due + 3 * hour
        assertFalse(DoseAdherence.isLate(taken(rescheduledTo + 10 * minute, rescheduledTo), threshold))
        assertTrue(DoseAdherence.isLate(taken(rescheduledTo + 2 * hour, rescheduledTo), threshold))
    }

    @Test
    fun `old doses without a taken time and doses that are not taken are never late`() {
        assertFalse(DoseAdherence.isLate(taken(null), threshold))
        assertFalse(DoseAdherence.isLate(dose(ReminderState.MISSED), threshold))
    }

    @Test
    fun `the threshold setting decides what counts as late`() {
        val dose = taken(due + 90 * minute)
        assertTrue(DoseAdherence.isLate(dose, 30))
        assertTrue(DoseAdherence.isLate(dose, 60))
        assertFalse(DoseAdherence.isLate(dose, 120))
    }

    @Test
    fun `a late dose counts as half a dose`() {
        val doses = listOf(taken(due), taken(due + 5 * minute), taken(null), taken(due + 3 * hour))
        assertEquals(87, DoseAdherence.percentage(doses, threshold))
    }

    @Test
    fun `only resolved non-placebo doses are counted`() {
        val doses = listOf(
            taken(due),
            dose(ReminderState.SKIPPED),
            dose(ReminderState.MISSED),
            taken(due + 3 * hour),
            dose(ReminderState.SCHEDULED),
            dose(ReminderState.DELIVERED),
            taken(due + 3 * hour, placebo = true)
        )
        // 1 zamanında + 1 geç (yarım) = 1,5 / 4
        assertEquals(37, DoseAdherence.percentage(doses, threshold))
    }

    @Test
    fun `no resolved doses means no percentage`() {
        assertNull(DoseAdherence.percentage(listOf(dose(ReminderState.SCHEDULED)), threshold))
        assertNull(DoseAdherence.percentage(emptyList(), threshold))
    }

    @Test
    fun `summary counts each outcome and averages the delay of doses with a taken time`() {
        val rescheduledTo = due + 3 * hour
        val summary = DoseAdherence.summarize(
            listOf(
                taken(due + 10 * minute),
                taken(due - 30 * minute),
                taken(due + 2 * hour + 20 * minute),
                taken(rescheduledTo + 30 * minute, rescheduledTo),
                taken(null),
                dose(ReminderState.SKIPPED),
                dose(ReminderState.MISSED),
                dose(ReminderState.SCHEDULED),
                taken(due + 5 * hour, placebo = true)
            ),
            threshold
        )
        assertEquals(5, summary.taken)
        assertEquals(1, summary.late)
        assertEquals(1, summary.skipped)
        assertEquals(1, summary.missed)
        assertEquals(7, summary.total)
        // 4 zamanında + 1 geç (yarım) = 4,5 / 7
        assertEquals(64, summary.percentage)
        // (10 + 0 + 140 + 30) / 4 — erken alınan 0 sayılır, alınma saati olmayan hesaba girmez
        assertEquals(45, summary.averageDelayMinutes)
    }

    @Test
    fun `summary has no average delay when no taken time is recorded`() {
        val summary = DoseAdherence.summarize(listOf(taken(null), dose(ReminderState.MISSED)), threshold)
        assertNull(summary.averageDelayMinutes)
        assertEquals(50, summary.percentage)
    }
}
