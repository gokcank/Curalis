package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.repository.DoctorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DoctorUseCases @Inject constructor(
    val getDoctors: GetDoctorsUseCase,
    val getDoctorById: GetDoctorByIdUseCase,
    val addDoctor: AddDoctorUseCase,
    val deleteDoctor: DeleteDoctorUseCase,
    val updateDoctor: UpdateDoctorUseCase
)

class GetDoctorsUseCase @Inject constructor(
    private val repository: DoctorRepository
) {
    operator fun invoke(): Flow<List<Doctor>> {
        return repository.getAllDoctors()
    }
}

class GetDoctorByIdUseCase @Inject constructor(
    private val repository: DoctorRepository
) {
    operator fun invoke(id: String): Flow<Doctor?> {
        return repository.getDoctorById(id)
    }
}

class AddDoctorUseCase @Inject constructor(
    private val repository: DoctorRepository
) {
    suspend operator fun invoke(doctor: Doctor) {
        repository.insertDoctor(doctor)
    }
}

class UpdateDoctorUseCase @Inject constructor(
    private val repository: DoctorRepository
) {
    suspend operator fun invoke(doctor: Doctor) {
        repository.updateDoctor(doctor)
    }
}

class DeleteDoctorUseCase @Inject constructor(
    private val repository: DoctorRepository
) {
    suspend operator fun invoke(doctor: Doctor) {
        repository.deleteDoctor(doctor)
    }
}
