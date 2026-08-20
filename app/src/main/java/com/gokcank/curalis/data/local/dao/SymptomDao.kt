package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.SymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    @Query("SELECT * FROM symptoms ORDER BY timeInMillis DESC")
    fun getAllSymptoms(): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE type = :type ORDER BY timeInMillis DESC")
    fun getSymptomsByType(type: String): Flow<List<SymptomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntity)
}
