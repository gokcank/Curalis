package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getAllMedications(): Flow<List<Medication>>
    fun getMedicationById(id: String): Flow<Medication?>
    fun searchMedications(query: String): Flow<List<Medication>>
    suspend fun insertMedication(medication: Medication)
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
}
