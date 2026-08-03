package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gokcank.curalis.data.local.entity.VitalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalDao {

    @Query("SELECT * FROM vitals ORDER BY timeInMillis DESC")
    fun getAllVitals(): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals WHERE type = :type ORDER BY timeInMillis DESC")
    fun getVitalsByType(type: String): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals WHERE id = :id")
    fun getVitalById(id: String): Flow<VitalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVital(vital: VitalEntity)

    @Update
    suspend fun updateVital(vital: VitalEntity)

    @Delete
    suspend fun deleteVital(vital: VitalEntity)
}
