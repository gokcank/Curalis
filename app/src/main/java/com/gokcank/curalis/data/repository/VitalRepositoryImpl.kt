package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.VitalDao
import com.gokcank.curalis.data.local.entity.VitalEntity
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.repository.VitalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitalRepositoryImpl @Inject constructor(
    private val vitalDao: VitalDao
) : VitalRepository {

    override fun getAllVitals(): Flow<List<Vital>> {
        return vitalDao.getAllVitals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVitalsByType(type: VitalType): Flow<List<Vital>> {
        return vitalDao.getVitalsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVitalById(id: String): Flow<Vital?> {
        return vitalDao.getVitalById(id).map { it?.toDomain() }
    }

    override suspend fun insertVital(vital: Vital) {
        vitalDao.insertVital(vital.toEntity())
    }

    override suspend fun updateVital(vital: Vital) {
        vitalDao.updateVital(vital.toEntity())
    }

    override suspend fun deleteVital(vital: Vital) {
        vitalDao.deleteVital(vital.toEntity())
    }
}

fun VitalEntity.toDomain(): Vital {
    return Vital(
        id = id,
        type = VitalType.valueOf(type),
        value1 = value1,
        value2 = value2,
        unit = unit,
        timeInMillis = timeInMillis,
        notes = notes
    )
}

fun Vital.toEntity(): VitalEntity {
    return VitalEntity(
        id = id,
        type = type.name,
        value1 = value1,
        value2 = value2,
        unit = unit,
        timeInMillis = timeInMillis,
        notes = notes
    )
}
