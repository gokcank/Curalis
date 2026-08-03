package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.Doctor
import kotlinx.coroutines.flow.Flow

interface DoctorRepository {
    fun getAllDoctors(): Flow<List<Doctor>>
    fun getDoctorById(id: String): Flow<Doctor?>
    suspend fun insertDoctor(doctor: Doctor)
    suspend fun updateDoctor(doctor: Doctor)
    suspend fun deleteDoctor(doctor: Doctor)
}
