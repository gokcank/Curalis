package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.VitalReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalReminderDao {

    @Query("SELECT * FROM vital_reminders")
    fun getAll(): Flow<List<VitalReminderEntity>>

    @Query("SELECT * FROM vital_reminders WHERE type = :type LIMIT 1")
    suspend fun getByType(type: String): VitalReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VitalReminderEntity)
}
