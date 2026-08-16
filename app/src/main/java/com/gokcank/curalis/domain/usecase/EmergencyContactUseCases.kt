package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.EmergencyContact
import com.gokcank.curalis.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EmergencyContactUseCases @Inject constructor(
    val getEmergencyContacts: GetEmergencyContactsUseCase,
    val getEmergencyContactById: GetEmergencyContactByIdUseCase,
    val addEmergencyContact: AddEmergencyContactUseCase,
    val updateEmergencyContact: UpdateEmergencyContactUseCase,
    val deleteEmergencyContact: DeleteEmergencyContactUseCase
)

class GetEmergencyContactsUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    operator fun invoke(): Flow<List<EmergencyContact>> {
        return repository.getAllEmergencyContacts()
    }
}

class GetEmergencyContactByIdUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    operator fun invoke(id: String): Flow<EmergencyContact?> {
        return repository.getEmergencyContactById(id)
    }
}

class AddEmergencyContactUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    suspend operator fun invoke(contact: EmergencyContact) {
        repository.insertEmergencyContact(contact)
    }
}

class UpdateEmergencyContactUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    suspend operator fun invoke(contact: EmergencyContact) {
        repository.updateEmergencyContact(contact)
    }
}

class DeleteEmergencyContactUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    suspend operator fun invoke(contact: EmergencyContact) {
        repository.deleteEmergencyContact(contact)
    }
}
