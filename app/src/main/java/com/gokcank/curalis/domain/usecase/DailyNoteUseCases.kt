package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.DailyNote
import com.gokcank.curalis.domain.repository.DailyNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DailyNoteUseCases @Inject constructor(
    val getNotes: GetDailyNotesUseCase,
    val getNoteForDate: GetDailyNoteForDateUseCase,
    val saveNote: SaveDailyNoteUseCase,
    val deleteNote: DeleteDailyNoteUseCase
)

class GetDailyNotesUseCase @Inject constructor(
    private val repository: DailyNoteRepository
) {
    operator fun invoke(): Flow<List<DailyNote>> {
        return repository.getAllNotes()
    }
}

class GetDailyNoteForDateUseCase @Inject constructor(
    private val repository: DailyNoteRepository
) {
    suspend operator fun invoke(dateMillis: Long): DailyNote? {
        return repository.getNoteForDate(dateMillis)
    }
}

class SaveDailyNoteUseCase @Inject constructor(
    private val repository: DailyNoteRepository
) {
    suspend operator fun invoke(note: DailyNote) {
        repository.saveNote(note)
    }
}

class DeleteDailyNoteUseCase @Inject constructor(
    private val repository: DailyNoteRepository
) {
    suspend operator fun invoke(note: DailyNote) {
        repository.deleteNote(note)
    }
}
