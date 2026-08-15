package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.core.utils.MedicationPhotoStorage
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
    private val repository: MedicationRepository,
    private val photoStorage: MedicationPhotoStorage
) {
    suspend operator fun invoke(medication: Medication) {
        repository.deleteMedication(medication)
        // Arşivlemede satır silinmediği için fotoğraf da silinmez; bu yalnızca kalıcı
        // silmede çalışır, aksi halde hâlâ görüntülenen bir arşiv kaydının fotoğrafı kaybolurdu.
        photoStorage.delete(medication.photoPath)
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

/** Arşivlenmiş bir ilacı Aktif sekmesine geri döndürür. Hatırlatıcı üretimi ve alarm
 *  kurulumu bunu çağıran tarafın sorumluluğundadır (bkz. MedicationListViewModel). */
class UnarchiveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.updateMedication(medication.copy(isArchived = false))
    }
}

/** İlacı geçici olarak duraklatır — arşivden farklı, ilaç listede kalır ama yeni
 *  hatırlatıcı üretilmez/alarm kurulmaz. */
class SuspendMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.updateMedication(medication.copy(isSuspended = true))
    }
}

/** Askıya alınmış bir ilacı devam ettirir. Hatırlatıcı üretimi ve alarm kurulumu
 *  bunu çağıran tarafın sorumluluğundadır (bkz. MedicationListViewModel). */
class ResumeMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.updateMedication(medication.copy(isSuspended = false))
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
