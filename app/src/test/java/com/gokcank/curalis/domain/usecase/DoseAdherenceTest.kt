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
}
