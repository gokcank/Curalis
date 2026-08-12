package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

private class FakeReminderRepository(private val reminders: List<Reminder>) : ReminderRepository {
    override suspend fun insertReminder(reminder: Reminder) {}
    override suspend fun insertReminders(reminders: List<Reminder>) {}
    override suspend fun updateReminder(reminder: Reminder) {}
    override suspend fun deleteReminder(reminder: Reminder) {}
    override suspend fun getReminderById(id: String): Reminder? = null
    override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> = flowOf(emptyList())
    override fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>> = flowOf(reminders)
}

private class FakeMedicationRepository(private val medications: List<Medication>) : MedicationRepository {
    override fun getAllMedications(): Flow<List<Medication>> = flowOf(medications)
    override fun getMedicationById(id: String): Flow<Medication?> = flowOf(medications.find { it.id == id })
    override fun searchMedications(query: String): Flow<List<Medication>> = flowOf(emptyList())
    override suspend fun insertMedication(medication: Medication) {}
    override suspend fun updateMedication(medication: Medication) {}
    override suspend fun deleteMedication(medication: Medication) {}
}

/**
 * "Sanal takvim üreticisi" (Madde: virtual schedule generator) hiçbir zaman doğrudan
 * kaydedilmeyen ama uyum ekranlarında görünmesi gereken planlanmış dozları üretir.
 * Uyum yüzdesi hesaplamaları doğrudan bu listeye dayandığı için doğruluğu kritik.
 */
class GetRemindersBetweenDatesUseCaseTest {

    private fun dayStart(daysFromNow: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysFromNow)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun dayEnd(daysFromNow: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysFromNow)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    @Test
    fun `AS_NEEDED medications never produce virtual reminders`() = runBlocking {
        val medication = Medication(
            name = "Ağrı kesici",
            frequencyType = FrequencyType.AS_NEEDED,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(emptyList()),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(0), dayEnd(3)).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `DAILY medication produces one virtual reminder per day in range`() = runBlocking {
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(emptyList()),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(0), dayEnd(3)).first()
        assertEquals(4, result.size) // gün 0, 1, 2, 3
        assertTrue(result.all { it.state == ReminderState.SCHEDULED })
    }

    @Test
    fun `does not duplicate a slot that already has a real database reminder`() = runBlocking {
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(-10)
        )
        val realReminderTime = Calendar.getInstance().apply {
            timeInMillis = dayStart(0)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
        val realReminder = Reminder(
            medicationId = medication.id,
            timeInMillis = realReminderTime,
            state = ReminderState.TAKEN
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(listOf(realReminder)),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(0), dayEnd(0)).first()
        assertEquals(1, result.size)
        assertEquals(ReminderState.TAKEN, result.first().state)
    }

    @Test
    fun `medication does not produce reminders before its start date`() = runBlocking {
        val medication = Medication(
            name = "Yeni başlanan ilaç",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(2)
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(emptyList()),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(0), dayEnd(3)).first()
        assertEquals(2, result.size) // yalnızca gün 2 ve 3
    }
}
