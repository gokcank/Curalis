package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.DailyNote
import kotlinx.coroutines.flow.Flow

interface DailyNoteRepository {
    fun getAllNotes(): Flow<List<DailyNote>>
    suspend fun getNoteForDate(dateMillis: Long): DailyNote?
    suspend fun saveNote(note: DailyNote)
    suspend fun deleteNote(note: DailyNote)
}
