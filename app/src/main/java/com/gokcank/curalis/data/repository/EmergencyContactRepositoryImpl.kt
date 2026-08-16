package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.EmergencyContactDao
import com.gokcank.curalis.data.local.entity.EmergencyContactEntity
import com.gokcank.curalis.domain.model.EmergencyContact
import com.gokcank.curalis.domain.repository.EmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepositoryImpl @Inject constructor(
    private val dao: EmergencyContactDao
) : EmergencyContactRepository {

    override fun getAllEmergencyContacts(): Flow<List<EmergencyContact>> {
        return dao.getAllEmergencyContacts().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getEmergencyContactById(id: String): Flow<EmergencyContact?> {
        return dao.getEmergencyContactById(id).map { it?.toDomain() }
    }

    override suspend fun insertEmergencyContact(contact: EmergencyContact) {
        dao.insertEmergencyContact(contact.toEntity())
    }

    override suspend fun updateEmergencyContact(contact: EmergencyContact) {
        dao.updateEmergencyContact(contact.toEntity())
    }

    override suspend fun deleteEmergencyContact(contact: EmergencyContact) {
        dao.deleteEmergencyContact(contact.toEntity())
    }
}

fun EmergencyContactEntity.toDomain(): EmergencyContact {
    return EmergencyContact(
        id = id,
        name = name,
        relationship = relationship,
        phoneNumber = phoneNumber,
        notes = notes
    )
}

fun EmergencyContact.toEntity(): EmergencyContactEntity {
    return EmergencyContactEntity(
        id = id,
        name = name,
        relationship = relationship,
        phoneNumber = phoneNumber,
        notes = notes
    )
}
