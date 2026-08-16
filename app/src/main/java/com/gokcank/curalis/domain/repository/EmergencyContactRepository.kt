package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {
    fun getAllEmergencyContacts(): Flow<List<EmergencyContact>>
    fun getEmergencyContactById(id: String): Flow<EmergencyContact?>
    suspend fun insertEmergencyContact(contact: EmergencyContact)
    suspend fun updateEmergencyContact(contact: EmergencyContact)
    suspend fun deleteEmergencyContact(contact: EmergencyContact)
}
