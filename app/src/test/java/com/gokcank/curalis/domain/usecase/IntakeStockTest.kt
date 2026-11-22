package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.LowStockNotifier
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

private class InMemoryMedicationRepository(initial: List<Medication>) : MedicationRepository {
    val medications = initial.associateBy { it.id }.toMutableMap()
    override fun getAllMedications(): Flow<List<Medication>> = flowOf(medications.values.toList())
    override fun getMedicationById(id: String): Flow<Medication?> = flowOf(medications[id])
    override fun searchMedications(query: String): Flow<List<Medication>> = flowOf(emptyList())
    override suspend fun insertMedication(medication: Medication) { medications[medication.id] = medication }
    override suspend fun updateMedication(medication: Medication) { medications[medication.id] = medication }
    override suspend fun deleteMedication(medication: Medication) { medications.remove(medication.id) }
}

private class InMemoryReminderRepository(initial: List<Reminder> = emptyList()) : ReminderRepository {
    val reminders = initial.associateBy { it.id }.toMutableMap()
    override suspend fun insertReminder(reminder: Reminder) { reminders[reminder.id] = reminder }
    override suspend fun insertReminders(reminders: List<Reminder>) { reminders.forEach { insertReminder(it) } }
    override suspend fun updateReminder(reminder: Reminder) { reminders[reminder.id] = reminder }
    override suspend fun deleteReminder(reminder: Reminder) { reminders.remove(reminder.id) }
    override suspend fun deleteUpcomingScheduledReminders(medicationId: String, after: Long) {}
    override suspend fun deleteScheduledRemindersFrom(medicationId: String, from: Long) {}
    override suspend fun getReminderById(id: String): Reminder? = reminders[id]
    override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> =
        flowOf(reminders.values.filter { it.medicationId == medicationId })
    override fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>> =
        flowOf(reminders.values.filter { it.timeInMillis in start..end })
}

private class RecordingStockHistoryRepository : StockHistoryRepository {
    val entries = mutableListOf<StockHistoryEntry>()
    override suspend fun logChange(entry: StockHistoryEntry) { entries += entry }
    override fun getHistoryForMedication(medicationId: String): Flow<List<StockHistoryEntry>> =
        flowOf(entries.filter { it.medicationId == medicationId })
    override fun getAllHistory(): Flow<List<StockHistoryEntry>> = flowOf(entries)
}

private class RecordingLowStockNotifier : LowStockNotifier {
    val alerts = mutableListOf<Pair<String, Int>>()
    override fun notifyLowStock(medicationName: String, remainingStock: Int) { alerts += medicationName to remainingStock }
}

/**
 * Stok, doz hangi ekrandan işaretlenirse işaretlensin aynı kurala uyar: "alındı" olunca
 * düşer, geri alınınca iade edilir; doz tam sayıysa o kadar, değilse 1 adet.
 */
class IntakeStockTest {

    private val zone = ZoneId.systemDefault()

    private fun todayAt(hour: Int, minute: Int): Long =
        LocalDate.now(zone).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun medication(
        stock: Int? = 10,
        dose: String? = null,
        refillEnabled: Boolean = false,
        threshold: Int? = null
    ) = Medication(
        id = "med",
        name = "Deneme",
        currentStock = stock,
        isRefillReminderEnabled = refillEnabled,
        refillThreshold = threshold,
        times = listOf(MedicationTime(hour = 8, minute = 0, dose = dose), MedicationTime(hour = 20, minute = 0, dose = "3"))
    )

    private class Fixture(med: Medication, initialReminders: List<Reminder> = emptyList()) {
        val medications = InMemoryMedicationRepository(listOf(med))
        val reminders = InMemoryReminderRepository(initialReminders)
        val history = RecordingStockHistoryRepository()
        val notifier = RecordingLowStockNotifier()
        val acknowledge = AcknowledgeReminderUseCase(
            reminders,
            ApplyIntakeStockChangeUseCase(medications, history, notifier)
        )
        fun stock() = medications.medications.getValue("med").currentStock
    }

    private fun dose(hour: Int = 8, state: ReminderState = ReminderState.SCHEDULED) =
        Reminder(id = "r$hour", medicationId = "med", timeInMillis = todayAt(hour, 0), state = state)

    @Test
    fun `amount is the whole-number dose, otherwise one`() {
        assertEquals(2, IntakeStockAmount.parse("2"))
        assertEquals(2, IntakeStockAmount.parse(" 2 "))
        assertEquals(1, IntakeStockAmount.parse("1,5"))
        assertEquals(1, IntakeStockAmount.parse("1.5"))
        assertEquals(1, IntakeStockAmount.parse("yarım"))
        assertEquals(1, IntakeStockAmount.parse("2 tablet"))
        assertEquals(1, IntakeStockAmount.parse(""))
        assertEquals(1, IntakeStockAmount.parse(null))
        assertEquals(1, IntakeStockAmount.parse("0"))
        assertEquals(1, IntakeStockAmount.parse("-2"))
    }

    @Test
    fun `amount follows the dose of the reminder's planned time`() {
        val med = medication(dose = "2")
        assertEquals(2, IntakeStockAmount.forReminder(med, dose(8), zone))
        assertEquals(3, IntakeStockAmount.forReminder(med, dose(20), zone))
        // Planı değişmiş eski bir saat: eşleşen saat yoksa 1.
        assertEquals(1, IntakeStockAmount.forReminder(med, dose(12), zone))
    }

    @Test
    fun `taking a stored dose lowers stock and logs it`() = runBlocking {
        val f = Fixture(medication(dose = "2"), listOf(dose(8)))
        assertTrue(f.acknowledge("r8", ReminderState.TAKEN, takenAtMillis = todayAt(8, 5)))
        assertEquals(8, f.stock())
        assertEquals(StockChangeReason.DOSE_TAKEN, f.history.entries.single().reason)
        assertEquals(10, f.history.entries.single().previousStock)
        assertEquals(8, f.history.entries.single().newStock)
    }

    @Test
    fun `taking a not-yet-stored dose from the screen lowers stock too`() = runBlocking {
        val f = Fixture(medication())
        f.acknowledge(dose(8), ReminderState.TAKEN, takenAtMillis = todayAt(8, 5))
        assertEquals(9, f.stock())
    }

    @Test
    fun `undoing a taken dose refunds the same amount`() = runBlocking {
        val f = Fixture(medication(dose = "2"), listOf(dose(8)))
        f.acknowledge("r8", ReminderState.TAKEN)
        f.acknowledge("r8", ReminderState.SCHEDULED)
        assertEquals(10, f.stock())
        assertEquals(StockChangeReason.DOSE_UNDONE, f.history.entries.last().reason)
    }

    @Test
    fun `marking an already taken dose taken again changes nothing`() = runBlocking {
        val f = Fixture(medication(), listOf(dose(8, ReminderState.TAKEN)))
        f.acknowledge("r8", ReminderState.TAKEN)
        assertEquals(10, f.stock())
        assertTrue(f.history.entries.isEmpty())
    }

    @Test
    fun `skip, snooze and missed do not touch stock`() = runBlocking {
        val f = Fixture(medication(), listOf(dose(8), dose(20)))
        f.acknowledge("r8", ReminderState.SKIPPED)
        f.acknowledge("r20", ReminderState.SNOOZED)
        f.acknowledge("r20", ReminderState.MISSED)
        assertEquals(10, f.stock())
        assertTrue(f.history.entries.isEmpty())
    }

    @Test
    fun `stock never goes below zero and empty stock is left alone`() = runBlocking {
        val f = Fixture(medication(stock = 1, dose = "2"), listOf(dose(8)))
        f.acknowledge("r8", ReminderState.TAKEN)
        assertEquals(0, f.stock())

        val empty = Fixture(medication(stock = 0), listOf(dose(8)))
        empty.acknowledge("r8", ReminderState.TAKEN)
        assertEquals(0, empty.stock())
        assertTrue(empty.history.entries.isEmpty())
    }

    @Test
    fun `no stock tracking means no change`() = runBlocking {
        val f = Fixture(medication(stock = null), listOf(dose(8)))
        f.acknowledge("r8", ReminderState.TAKEN)
        assertNull(f.stock())
        assertTrue(f.history.entries.isEmpty())
    }

    @Test
    fun `low stock alert fires at or below the threshold only when enabled`() = runBlocking {
        val enabled = Fixture(medication(stock = 10, refillEnabled = true, threshold = 9), listOf(dose(8)))
        enabled.acknowledge("r8", ReminderState.TAKEN)
        assertEquals(listOf("Deneme" to 9), enabled.notifier.alerts)

        val above = Fixture(medication(stock = 10, refillEnabled = true, threshold = 5), listOf(dose(8)))
        above.acknowledge("r8", ReminderState.TAKEN)
        assertTrue(above.notifier.alerts.isEmpty())

        val disabled = Fixture(medication(stock = 3, refillEnabled = false, threshold = 9), listOf(dose(8)))
        disabled.acknowledge("r8", ReminderState.TAKEN)
        assertTrue(disabled.notifier.alerts.isEmpty())
    }

    @Test
    fun `unknown reminder id changes nothing`() = runBlocking {
        val f = Fixture(medication())
        assertEquals(false, f.acknowledge("missing", ReminderState.TAKEN))
        assertEquals(10, f.stock())
    }
}
