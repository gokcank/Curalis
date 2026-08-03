package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import kotlinx.coroutines.flow.Flow

interface VitalRepository {
    fun getAllVitals(): Flow<List<Vital>>
    fun getVitalsByType(type: VitalType): Flow<List<Vital>>
    fun getVitalById(id: String): Flow<Vital?>
    suspend fun insertVital(vital: Vital)
    suspend fun updateVital(vital: Vital)
    suspend fun deleteVital(vital: Vital)
}
