package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.DailyNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyNoteDao {

    @Query("SELECT * FROM daily_notes ORDER BY dateMillis DESC")
    fun getAllNotes(): Flow<List<DailyNoteEntity>>

    @Query("SELECT * FROM daily_notes WHERE dateMillis = :dateMillis LIMIT 1")
    suspend fun getNoteForDate(dateMillis: Long): DailyNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: DailyNoteEntity)

    @Delete
    suspend fun delete(note: DailyNoteEntity)
}
