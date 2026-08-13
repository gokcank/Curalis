package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

private class RecordingReminderRepository : ReminderRepository {
    val inserted = mutableListOf<Reminder>()
    override suspend fun insertReminder(reminder: Reminder) { inserted.add(reminder) }
    override suspend fun insertReminders(reminders: List<Reminder>) { inserted.addAll(reminders) }
    override suspend fun updateReminder(reminder: Reminder) {}
    override suspend fun deleteReminder(reminder: Reminder) {}
    override suspend fun getReminderById(id: String): Reminder? = null
    override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> = flowOf(emptyList())
    override fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>> = flowOf(emptyList())
}

class GenerateUpcomingRemindersUseCaseTest {

    private fun dayStart(daysFromNow: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysFromNow)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun `AS_NEEDED medication generates nothing`() = runBlocking {
        val repository = RecordingReminderRepository()
        val medication = Medication(
            name = "Ağrı kesici",
            frequencyType = FrequencyType.AS_NEEDED,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )

        GenerateUpcomingRemindersUseCase(repository)(medication)

        assertTrue(repository.inserted.isEmpty())
    }

    @Test
    fun `archived medication generates nothing`() = runBlocking {
        val repository = RecordingReminderRepository()
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10),
            isArchived = true
        )

        GenerateUpcomingRemindersUseCase(repository)(medication)

        assertTrue(repository.inserted.isEmpty())
    }

    @Test
    fun `DAILY medication generates one reminder per day for the whole window`() = runBlocking {
        val repository = RecordingReminderRepository()
        val fromMillis = dayStart(0)
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )

        GenerateUpcomingRemindersUseCase(repository)(medication, fromMillis)

        assertEquals(GenerateUpcomingRemindersUseCase.WINDOW_DAYS, repository.inserted.size)
        assertTrue(repository.inserted.all { it.state == ReminderState.SCHEDULED })
        assertTrue(repository.inserted.all { it.medicationId == medication.id })
    }

    @Test
    fun `generated reminder ids are deterministic and stable across recomputation`() = runBlocking {
        val repository = RecordingReminderRepository()
        val fromMillis = dayStart(0)
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )
        val useCase = GenerateUpcomingRemindersUseCase(repository)

        useCase(medication, fromMillis)
        val firstRunIds = repository.inserted.map { it.id }.toSet()
        repository.inserted.clear()
        useCase(medication, fromMillis)
        val secondRunIds = repository.inserted.map { it.id }.toSet()

        assertEquals(firstRunIds, secondRunIds)
    }

    @Test
    fun `already passed times today are not regenerated`() = runBlocking {
        val repository = RecordingReminderRepository()
        val now = Calendar.getInstance().apply {
            timeInMillis = dayStart(0)
            set(Calendar.HOUR_OF_DAY, 14)
        }.timeInMillis
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )

        GenerateUpcomingRemindersUseCase(repository)(medication, now)

        // Bugünün 09:00'ı zaten geçtiği için WINDOW_DAYS'ten bir eksik üretilmeli (bugün hariç).
        assertEquals(GenerateUpcomingRemindersUseCase.WINDOW_DAYS - 1, repository.inserted.size)
    }
}
