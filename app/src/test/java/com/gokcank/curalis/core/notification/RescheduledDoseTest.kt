package com.gokcank.curalis.core.notification

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.usecase.RescheduleDoseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescheduledDoseTest {

    private val now = 1_000_000L
    private fun dose(state: ReminderState, rescheduledTo: Long? = null) =
        Reminder(id = "d1", medicationId = "m1", timeInMillis = now, state = state, rescheduledToMillis = rescheduledTo)

    @Test
    fun `regular alarm stays silent for a dose rescheduled to later`() {
        assertFalse(shouldNotifyDose(dose(ReminderState.SCHEDULED, rescheduledTo = now + 60_000), false, now))
    }

    @Test
    fun `regular alarm stays silent for answered doses`() {
        assertFalse(shouldNotifyDose(dose(ReminderState.TAKEN), false, now))
        assertFalse(shouldNotifyDose(dose(ReminderState.SKIPPED), false, now))
    }

    @Test
    fun `regular alarm does not repeat a dose already reminded at its earlier new time`() {
        assertFalse(shouldNotifyDose(dose(ReminderState.DELIVERED, rescheduledTo = now - 60_000), false, now))
    }

    @Test
    fun `regular alarm still reminds when the rescheduled reminder never came`() {
        assertTrue(shouldNotifyDose(dose(ReminderState.SCHEDULED, rescheduledTo = now - 60_000), false, now))
    }

    @Test
    fun `rescheduled alarm reminds only while the dose is still waiting`() {
        assertTrue(shouldNotifyDose(dose(ReminderState.SCHEDULED, rescheduledTo = now), true, now))
        assertFalse(shouldNotifyDose(dose(ReminderState.TAKEN, rescheduledTo = now), true, now))
        assertFalse(shouldNotifyDose(null, true, now))
    }

    @Test
    fun `rescheduling a delivered dose puts it back to waiting with the new time`() = runBlocking {
        val repository = object : ReminderRepository {
            var stored: Reminder? = dose(ReminderState.DELIVERED)
            override suspend fun insertReminder(reminder: Reminder) { stored = reminder }
            override suspend fun insertReminders(reminders: List<Reminder>) {}
            override suspend fun updateReminder(reminder: Reminder) { stored = reminder }
            override suspend fun deleteReminder(reminder: Reminder) {}
            override suspend fun deleteUpcomingScheduledReminders(medicationId: String, after: Long) {}
            override suspend fun deleteScheduledRemindersFrom(medicationId: String, from: Long) {}
            override suspend fun getReminderById(id: String): Reminder? = stored
            override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> = flowOf(emptyList())
            override fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>> = flowOf(emptyList())
        }
        RescheduleDoseUseCase(repository)(dose(ReminderState.DELIVERED), now + 3_600_000)
        assertEquals(ReminderState.SCHEDULED, repository.stored?.state)
        assertEquals(now + 3_600_000, repository.stored?.rescheduledToMillis)
        assertEquals(now, repository.stored?.timeInMillis)
    }
}
