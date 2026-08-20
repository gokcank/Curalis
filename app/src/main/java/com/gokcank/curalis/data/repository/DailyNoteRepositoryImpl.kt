package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.DailyNoteDao
import com.gokcank.curalis.data.local.entity.DailyNoteEntity
import com.gokcank.curalis.domain.model.DailyNote
import com.gokcank.curalis.domain.model.Mood
import com.gokcank.curalis.domain.repository.DailyNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyNoteRepositoryImpl @Inject constructor(
    private val dao: DailyNoteDao
) : DailyNoteRepository {

    override fun getAllNotes(): Flow<List<DailyNote>> {
        return dao.getAllNotes().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getNoteForDate(dateMillis: Long): DailyNote? {
        return dao.getNoteForDate(dateMillis)?.toDomain()
    }

    override suspend fun saveNote(note: DailyNote) {
        dao.upsert(note.toEntity())
    }

    override suspend fun deleteNote(note: DailyNote) {
        dao.delete(note.toEntity())
    }
}

fun DailyNoteEntity.toDomain(): DailyNote {
    return DailyNote(
        dateMillis = dateMillis,
        content = content,
        mood = mood?.let { runCatching { Mood.valueOf(it) }.getOrNull() }
    )
}

fun DailyNote.toEntity(): DailyNoteEntity {
    return DailyNoteEntity(
        dateMillis = dateMillis,
        content = content,
        mood = mood?.name
    )
}
