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
    override suspend fun deleteUpcomingScheduledReminders(medicationId: String, after: Long) {}
    override suspend fun deleteScheduledRemindersFrom(medicationId: String, from: Long) {}
    override suspend fun getReminderById(id: String): Reminder? = null
    override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> = flowOf(emptyList())
    override fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>> =
        flowOf(reminders.filter { it.timeInMillis in start..end })
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
    fun `does not add a virtual reminder at the medication's new time when today already has a real reminder at the old time`() = runBlocking {
        // Saat değişikliği "yarından itibaren" uygulandığında, bugünün eski saatteki
        // gerçek kaydı bilinçli olarak korunur ve ilacın güncel (yeni) saati o gün
        // için hiç veritabanına yazılmaz. Bu durumda ekran hesaplaması, bugünü ilacın
        // yeni saatiyle tekrar doldurup çift doz göstermemeli.
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 21, minute = 0)), // güncel (yeni) saat
            startDate = dayStart(-10)
        )
        val oldTimeToday = Calendar.getInstance().apply {
            timeInMillis = dayStart(0)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
        val realReminderAtOldTime = Reminder(
            medicationId = medication.id,
            timeInMillis = oldTimeToday,
            state = ReminderState.SCHEDULED
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(listOf(realReminderAtOldTime)),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(0), dayEnd(0)).first()
        assertEquals(1, result.size)
        assertEquals(oldTimeToday, result.first().timeInMillis)
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

    @Test
    fun `no virtual reminder for the new time before it takes effect`() = runBlocking {
        val medication = Medication(
            name = "Tansiyon ilacı",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 21, minute = 0)),
            startDate = dayStart(-10),
            timesEffectiveFrom = dayStart(1)
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(emptyList()),
            FakeMedicationRepository(listOf(medication))
        )

        val today = useCase(dayStart(0), dayEnd(0)).first()
        val tomorrow = useCase(dayStart(1), dayEnd(1)).first()

        assertTrue(today.isEmpty())
        assertEquals(1, tomorrow.size)
    }

    private fun at(daysFromNow: Int, hour: Int): Long = dayStart(daysFromNow) + hour * 3_600_000L

    @Test
    fun `days after the 30-day window are filled from the plan without gaps or duplicates`() = runBlocking {
        val medication = Medication(
            id = "m1",
            name = "Günlük ilaç",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = 9, minute = 0), MedicationTime(hour = 21, minute = 0)),
            startDate = dayStart(-10)
        )
        // Veritabanında yalnızca 30 günlük pencere var (gün 1..29).
        val stored = (1..29).flatMap { day ->
            listOf(9, 21).map { hour -> Reminder(id = "m1_${at(day, hour)}", medicationId = "m1", timeInMillis = at(day, hour)) }
        }
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(stored),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(27), dayEnd(40)).first()
        val expected = (27..40).flatMap { day -> listOf(at(day, 9), at(day, 21)) }
        assertEquals(expected, result.map { it.timeInMillis })
    }

    @Test
    fun `upcoming stored doses of a suspended or archived medication are hidden, past ones kept`() = runBlocking {
        val suspended = Medication(id = "s", name = "Askıda", times = listOf(MedicationTime(hour = 9, minute = 0)), startDate = dayStart(-10), isSuspended = true)
        val archived = Medication(id = "a", name = "Arşivde", times = listOf(MedicationTime(hour = 9, minute = 0)), startDate = dayStart(-10), isArchived = true)
        val stored = listOf(
            Reminder(id = "s-past", medicationId = "s", timeInMillis = at(-1, 9), state = ReminderState.TAKEN),
            Reminder(id = "s-past-open", medicationId = "s", timeInMillis = at(-1, 10)),
            Reminder(id = "s-future", medicationId = "s", timeInMillis = at(2, 9)),
            Reminder(id = "a-future", medicationId = "a", timeInMillis = at(3, 9))
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(stored),
            FakeMedicationRepository(listOf(suspended, archived))
        )

        val result = useCase(dayStart(-1), dayEnd(5)).first()
        assertEquals(listOf("s-past", "s-past-open"), result.map { it.id })
    }

    @Test
    fun `virtual doses on rest days of a cyclic medication are marked as placebo`() = runBlocking {
        val medication = Medication(
            name = "Doğum kontrol",
            frequencyType = FrequencyType.CYCLIC,
            activeDays = 2,
            restDays = 1,
            hasPlaceboDays = true,
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(40)
        )
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(emptyList()),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(40), dayEnd(45)).first()
        assertEquals(listOf(false, false, true, false, false, true), result.map { it.isPlacebo })
    }

    @Test
    fun `a dose rescheduled to another day stays on its own day`() = runBlocking {
        val medication = Medication(id = "m1", name = "Akşam ilacı", times = listOf(MedicationTime(hour = 21, minute = 0)), startDate = dayStart(-10))
        val moved = Reminder(id = "m1_${at(2, 21)}", medicationId = "m1", timeInMillis = at(2, 21), rescheduledToMillis = at(3, 8))
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(listOf(moved)),
            FakeMedicationRepository(listOf(medication))
        )

        val day2 = useCase(dayStart(2), dayEnd(2)).first()
        assertEquals(listOf(moved), day2)
    }

    @Test
    fun `no doses are shown after the treatment ends, including stored ones`() = runBlocking {
        val medication = Medication(
            id = "m1",
            name = "Antibiyotik",
            times = listOf(MedicationTime(hour = 9, minute = 0)),
            startDate = dayStart(0),
            treatmentDurationDays = 3
        )
        val storedAfterEnd = Reminder(id = "m1_${at(4, 9)}", medicationId = "m1", timeInMillis = at(4, 9))
        val takenOnLastDay = Reminder(id = "m1_${at(2, 9)}", medicationId = "m1", timeInMillis = at(2, 9), state = ReminderState.TAKEN)
        val useCase = GetRemindersBetweenDatesUseCase(
            FakeReminderRepository(listOf(takenOnLastDay, storedAfterEnd)),
            FakeMedicationRepository(listOf(medication))
        )

        val result = useCase(dayStart(0), dayEnd(6)).first()
        assertEquals(listOf(at(0, 9), at(1, 9), at(2, 9)), result.map { it.timeInMillis })
    }
}
