package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ScheduleChangeCleanupTest {

    private val startOfToday = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 13, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun at(hour: Int, minute: Int, daysFromToday: Int = 0): Long = Calendar.getInstance().apply {
        timeInMillis = startOfToday
        add(Calendar.DAY_OF_YEAR, daysFromToday)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.timeInMillis

    private fun dose(id: String, time: Long, state: ReminderState = ReminderState.DELIVERED) =
        Reminder(id = id, medicationId = "m1", timeInMillis = time, state = state)

    private fun select(reminders: List<Reminder>, old: List<MedicationTime>, new: List<MedicationTime>, now: Long) =
        ScheduleChangeCleanup.staleUnansweredDosesToday(reminders, old, new, now, startOfToday).map { it.id }

    @Test
    fun `due unanswered dose at a moved time is selected`() {
        val reminders = listOf(dose("d1900", at(19, 0)))
        val selected = select(reminders, listOf(MedicationTime(hour = 19, minute = 0)), listOf(MedicationTime(hour = 19, minute = 30)), now = at(19, 10))
        assertEquals(listOf("d1900"), selected)
    }

    @Test
    fun `answered doses and doses at unchanged times are kept`() {
        val reminders = listOf(
            dose("taken", at(8, 0), ReminderState.TAKEN),
            dose("skipped", at(8, 0), ReminderState.SKIPPED),
            dose("unchanged", at(13, 0))
        )
        val old = listOf(MedicationTime(hour = 8, minute = 0), MedicationTime(hour = 13, minute = 0))
        val new = listOf(MedicationTime(hour = 9, minute = 0), MedicationTime(hour = 13, minute = 0))
        assertEquals(emptyList<String>(), select(reminders, old, new, now = at(14, 0)))
    }

    @Test
    fun `snoozed dose and doses from other days are kept`() {
        val reminders = listOf(
            dose("snoozed", at(19, 10), ReminderState.SCHEDULED),
            dose("yesterday", at(19, 0, daysFromToday = -1)),
            dose("future", at(21, 0, daysFromToday = 1), ReminderState.SCHEDULED)
        )
        val selected = select(reminders, listOf(MedicationTime(hour = 19, minute = 0)), listOf(MedicationTime(hour = 19, minute = 30)), now = at(19, 15))
        assertEquals(emptyList<String>(), selected)
    }

    @Test
    fun `nothing is selected when only times were added`() {
        val reminders = listOf(dose("d1900", at(19, 0)))
        val old = listOf(MedicationTime(hour = 19, minute = 0))
        val new = listOf(MedicationTime(hour = 19, minute = 0), MedicationTime(hour = 22, minute = 0))
        assertEquals(emptyList<String>(), select(reminders, old, new, now = at(19, 10)))
    }
}
