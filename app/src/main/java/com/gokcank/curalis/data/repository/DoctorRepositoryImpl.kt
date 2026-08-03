package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.DoctorDao
import com.gokcank.curalis.data.local.entity.DoctorEntity
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.repository.DoctorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepositoryImpl @Inject constructor(
    private val doctorDao: DoctorDao
) : DoctorRepository {

    override fun getAllDoctors(): Flow<List<Doctor>> {
        return doctorDao.getAllDoctors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDoctorById(id: String): Flow<Doctor?> {
        return doctorDao.getDoctorById(id).map { it?.toDomain() }
    }

    override suspend fun insertDoctor(doctor: Doctor) {
        doctorDao.insertDoctor(doctor.toEntity())
    }

    override suspend fun updateDoctor(doctor: Doctor) {
        doctorDao.updateDoctor(doctor.toEntity())
    }

    override suspend fun deleteDoctor(doctor: Doctor) {
        doctorDao.deleteDoctor(doctor.toEntity())
    }
}

fun DoctorEntity.toDomain(): Doctor {
    return Doctor(
        id = id,
        name = name,
        specialty = specialty,
        phoneNumber = phoneNumber,
        email = email,
        hospitalName = hospitalName,
        notes = notes
    )
}

fun Doctor.toEntity(): DoctorEntity {
    return DoctorEntity(
        id = id,
        name = name,
        specialty = specialty,
        phoneNumber = phoneNumber,
        email = email,
        hospitalName = hospitalName,
        notes = notes
    )
}
