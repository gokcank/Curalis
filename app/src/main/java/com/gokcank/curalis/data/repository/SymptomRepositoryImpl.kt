package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.SymptomDao
import com.gokcank.curalis.data.local.entity.SymptomEntity
import com.gokcank.curalis.domain.model.Symptom
import com.gokcank.curalis.domain.model.SymptomType
import com.gokcank.curalis.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymptomRepositoryImpl @Inject constructor(
    private val symptomDao: SymptomDao
) : SymptomRepository {

    override fun getAllSymptoms(): Flow<List<Symptom>> {
        return symptomDao.getAllSymptoms().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSymptomsByType(type: SymptomType): Flow<List<Symptom>> {
        return symptomDao.getSymptomsByType(type.name).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertSymptom(symptom: Symptom) {
        symptomDao.insertSymptom(symptom.toEntity())
    }

    override suspend fun deleteSymptom(symptom: Symptom) {
        symptomDao.deleteSymptom(symptom.toEntity())
    }
}

fun SymptomEntity.toDomain(): Symptom {
    return Symptom(
        id = id,
        type = SymptomType.valueOf(type),
        severity = severity,
        timeInMillis = timeInMillis,
        notes = notes
    )
}

fun Symptom.toEntity(): SymptomEntity {
    return SymptomEntity(
        id = id,
        type = type.name,
        severity = severity,
        timeInMillis = timeInMillis,
        notes = notes
    )
}
