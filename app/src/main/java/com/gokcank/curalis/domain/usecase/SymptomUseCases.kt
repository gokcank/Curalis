package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Symptom
import com.gokcank.curalis.domain.model.SymptomType
import com.gokcank.curalis.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SymptomUseCases @Inject constructor(
    val getSymptoms: GetSymptomsUseCase,
    val getSymptomsByType: GetSymptomsByTypeUseCase,
    val addSymptom: AddSymptomUseCase,
    val deleteSymptom: DeleteSymptomUseCase
)

class GetSymptomsUseCase @Inject constructor(
    private val repository: SymptomRepository
) {
    operator fun invoke(): Flow<List<Symptom>> {
        return repository.getAllSymptoms()
    }
}

class GetSymptomsByTypeUseCase @Inject constructor(
    private val repository: SymptomRepository
) {
    operator fun invoke(type: SymptomType): Flow<List<Symptom>> {
        return repository.getSymptomsByType(type)
    }
}

class AddSymptomUseCase @Inject constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(symptom: Symptom) {
        repository.insertSymptom(symptom)
    }
}

class DeleteSymptomUseCase @Inject constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(symptom: Symptom) {
        repository.deleteSymptom(symptom)
    }
}
