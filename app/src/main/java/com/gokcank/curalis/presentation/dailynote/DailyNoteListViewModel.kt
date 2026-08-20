package com.gokcank.curalis.presentation.dailynote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.DailyNote
import com.gokcank.curalis.domain.usecase.DailyNoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyNoteListViewModel @Inject constructor(
    private val dailyNoteUseCases: DailyNoteUseCases
) : ViewModel() {

    val notes: StateFlow<List<DailyNote>> = dailyNoteUseCases.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteNote(note: DailyNote) {
        viewModelScope.launch {
            dailyNoteUseCases.deleteNote(note)
        }
    }
}
