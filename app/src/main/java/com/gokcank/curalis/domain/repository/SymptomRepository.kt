package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.Symptom
import com.gokcank.curalis.domain.model.SymptomType
import kotlinx.coroutines.flow.Flow

interface SymptomRepository {
    fun getAllSymptoms(): Flow<List<Symptom>>
    fun getSymptomsByType(type: SymptomType): Flow<List<Symptom>>
    suspend fun insertSymptom(symptom: Symptom)
    suspend fun deleteSymptom(symptom: Symptom)
}
