package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gokcank.curalis.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: String): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR activeIngredient LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMedications(query: String): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("SELECT * FROM medication_days WHERE medicationId = :medicationId")
    fun getMedicationDays(medicationId: String): Flow<List<com.gokcank.curalis.data.local.entity.MedicationDaysEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationDays(days: List<com.gokcank.curalis.data.local.entity.MedicationDaysEntity>)

    @Query("DELETE FROM medication_days WHERE medicationId = :medicationId")
    suspend fun deleteMedicationDays(medicationId: String)

    @Query("SELECT * FROM medication_times WHERE medicationId = :medicationId")
    fun getMedicationTimes(medicationId: String): Flow<List<com.gokcank.curalis.data.local.entity.MedicationTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationTimes(times: List<com.gokcank.curalis.data.local.entity.MedicationTimeEntity>)

    @Query("DELETE FROM medication_times WHERE medicationId = :medicationId")
    suspend fun deleteMedicationTimes(medicationId: String)
}
