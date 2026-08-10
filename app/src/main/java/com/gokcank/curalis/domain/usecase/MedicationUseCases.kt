package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.repository.MedicationRepository
import javax.inject.Inject

class AddMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.insertMedication(medication)
    }
}

class UpdateMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.updateMedication(medication)
    }
}

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.deleteMedication(medication)
    }
}

/**
 * İlacı gerçekten silmek yerine aktif listelerden gizler ("arşivler"). Geçmiş
 * hatırlatıcı kayıtları (alındı/atlandı/kaçırıldı) etkilenmez; bu sayede uyum
 * geçmişi ilaç silindikten sonra da anlamlı kalır.
 */
class ArchiveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.updateMedication(medication.copy(isArchived = true))
    }
}

class GetMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke() = repository.getAllMedications()
}

class GetMedicationByIdUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(id: String) = repository.getMedicationById(id)
}

class SearchMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(query: String) = repository.searchMedications(query)
}

class ValidateMedicationUseCase @Inject constructor() {
    operator fun invoke(name: String): Boolean {
        return name.isNotBlank()
    }
}
