package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustStockUseCaseTest {

    private class Meds(initial: List<Medication>) : MedicationRepository {
        val medications = initial.associateBy { it.id }.toMutableMap()
        override fun getAllMedications(): Flow<List<Medication>> = flowOf(medications.values.toList())
        override fun getMedicationById(id: String): Flow<Medication?> = flowOf(medications[id])
        override fun searchMedications(query: String): Flow<List<Medication>> = flowOf(emptyList())
        override suspend fun insertMedication(medication: Medication) { medications[medication.id] = medication }
        override suspend fun updateMedication(medication: Medication) { medications[medication.id] = medication }
        override suspend fun deleteMedication(medication: Medication) { medications.remove(medication.id) }
    }

    private class History : StockHistoryRepository {
        val entries = mutableListOf<StockHistoryEntry>()
        override suspend fun logChange(entry: StockHistoryEntry) { entries += entry }
        override fun getHistoryForMedication(medicationId: String): Flow<List<StockHistoryEntry>> = flowOf(entries)
        override fun getAllHistory(): Flow<List<StockHistoryEntry>> = flowOf(entries)
    }

    private fun setup(vararg meds: Medication): Triple<AdjustStockUseCase, Meds, History> {
        val repo = Meds(meds.toList())
        val history = History()
        return Triple(AdjustStockUseCase(repo, history), repo, history)
    }

    @Test
    fun `adding a box adds the initial stock and records a refill`() = runBlocking {
        val (useCase, repo, history) = setup(Medication(id = "m", name = "A", initialStock = 30, currentStock = 4))

        assertTrue(useCase.addBox("m"))

        assertEquals(34, repo.medications["m"]?.currentStock)
        assertEquals(30, repo.medications["m"]?.initialStock)
        val entry = history.entries.single()
        assertEquals(StockChangeReason.REFILL, entry.reason)
        assertEquals(4, entry.previousStock)
        assertEquals(34, entry.newStock)
    }

    @Test
    fun `no box can be added without an initial stock`() = runBlocking {
        val (useCase, repo, history) = setup(Medication(id = "m", name = "A", initialStock = null, currentStock = 4))
        assertFalse(useCase.addBox("m"))
        assertEquals(4, repo.medications["m"]?.currentStock)
        assertTrue(history.entries.isEmpty())
    }

    @Test
    fun `entering a stock records a refill when it rises and an edit when it falls`() = runBlocking {
        val (useCase, repo, history) = setup(Medication(id = "m", name = "A", initialStock = 30, currentStock = 4))

        useCase.setStock("m", 10)
        useCase.setStock("m", 7)
        useCase.setStock("m", 7) // unchanged: nothing recorded

        assertEquals(7, repo.medications["m"]?.currentStock)
        assertEquals(listOf(StockChangeReason.REFILL, StockChangeReason.MANUAL_EDIT), history.entries.map { it.reason })
    }

    @Test
    fun `entered stock never goes below zero`() = runBlocking {
        val (useCase, repo, _) = setup(Medication(id = "m", name = "A", currentStock = 4))
        useCase.setStock("m", -3)
        assertEquals(0, repo.medications["m"]?.currentStock)
    }
}
