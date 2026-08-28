package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.MedicationDao
import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.mapper.toDomain
import com.gokcank.curalis.data.mapper.toEntity
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val dao: MedicationDao
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> {
        return dao.getAllMedications().map { entities ->
            entities.map { entity ->
                val times = dao.getMedicationTimes(entity.id).firstOrNull() ?: emptyList()
                val days = dao.getMedicationDays(entity.id).firstOrNull() ?: emptyList()
                entity.toDomain(times = times.map { it.toDomain() }, specificDays = days.map { it.dayOfWeek })
            }
        }
    }

    override fun getMedicationById(id: String): Flow<Medication?> {
        return dao.getMedicationById(id).map { entity ->
            if (entity == null) null
            else {
                val times = dao.getMedicationTimes(entity.id).firstOrNull() ?: emptyList()
                val days = dao.getMedicationDays(entity.id).firstOrNull() ?: emptyList()
                entity.toDomain(times = times.map { it.toDomain() }, specificDays = days.map { it.dayOfWeek })
            }
        }
    }

    override fun searchMedications(query: String): Flow<List<Medication>> {
        return dao.searchMedications(query).map { entities ->
            entities.map { entity ->
                val times = dao.getMedicationTimes(entity.id).firstOrNull() ?: emptyList()
                val days = dao.getMedicationDays(entity.id).firstOrNull() ?: emptyList()
                entity.toDomain(times = times.map { it.toDomain() }, specificDays = days.map { it.dayOfWeek })
            }
        }
    }

    override suspend fun insertMedication(medication: Medication) {
        dao.insertMedication(medication.toEntity())
        dao.deleteMedicationTimes(medication.id)
        if (medication.times.isNotEmpty()) {
            dao.insertMedicationTimes(medication.times.map { it.toEntity(medication.id) })
        }
        dao.deleteMedicationDays(medication.id)
        if (medication.specificDays.isNotEmpty()) {
            dao.insertMedicationDays(medication.specificDays.map { MedicationDaysEntity(medication.id, it) })
        }
    }

    override suspend fun updateMedication(medication: Medication) {
        dao.updateMedication(medication.toEntity())
        dao.deleteMedicationTimes(medication.id)
        if (medication.times.isNotEmpty()) {
            dao.insertMedicationTimes(medication.times.map { it.toEntity(medication.id) })
        }
        dao.deleteMedicationDays(medication.id)
        if (medication.specificDays.isNotEmpty()) {
            dao.insertMedicationDays(medication.specificDays.map { MedicationDaysEntity(medication.id, it) })
        }
    }

    override suspend fun deleteMedication(medication: Medication) {
        dao.deleteMedicationTimes(medication.id)
        dao.deleteMedicationDays(medication.id)
        dao.deleteMedication(medication.toEntity())
    }
}
