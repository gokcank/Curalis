package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gokcank.curalis.data.local.entity.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAllEmergencyContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    fun getEmergencyContactById(id: String): Flow<EmergencyContactEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContactEntity)

    @Update
    suspend fun updateEmergencyContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteEmergencyContact(contact: EmergencyContactEntity)
}
