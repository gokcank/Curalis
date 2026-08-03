package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.repository.VitalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VitalUseCases @Inject constructor(
    val getVitals: GetVitalsUseCase,
    val getVitalsByType: GetVitalsByTypeUseCase,
    val getVitalById: GetVitalByIdUseCase,
    val addVital: AddVitalUseCase,
    val deleteVital: DeleteVitalUseCase
)

class GetVitalsUseCase @Inject constructor(
    private val repository: VitalRepository
) {
    operator fun invoke(): Flow<List<Vital>> {
        return repository.getAllVitals()
    }
}

class GetVitalsByTypeUseCase @Inject constructor(
    private val repository: VitalRepository
) {
    operator fun invoke(type: VitalType): Flow<List<Vital>> {
        return repository.getVitalsByType(type)
    }
}

class GetVitalByIdUseCase @Inject constructor(
    private val repository: VitalRepository
) {
    operator fun invoke(id: String): Flow<Vital?> {
        return repository.getVitalById(id)
    }
}

class AddVitalUseCase @Inject constructor(
    private val repository: VitalRepository
) {
    suspend operator fun invoke(vital: Vital) {
        repository.insertVital(vital)
    }
}

class DeleteVitalUseCase @Inject constructor(
    private val repository: VitalRepository
) {
    suspend operator fun invoke(vital: Vital) {
        repository.deleteVital(vital)
    }
}
