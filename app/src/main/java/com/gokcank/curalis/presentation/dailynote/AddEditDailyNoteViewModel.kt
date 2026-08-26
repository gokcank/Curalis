package com.gokcank.curalis.presentation.dailynote

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.DailyNote
import com.gokcank.curalis.domain.model.Mood
import com.gokcank.curalis.domain.usecase.DailyNoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditDailyNoteViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dailyNoteUseCases: DailyNoteUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val dateMillis: Long = checkNotNull(savedStateHandle.get<Long>("dateMillis"))

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _mood = MutableStateFlow<Mood?>(null)
    val mood = _mood.asStateFlow()

    /** Bu tarih için daha önce kaydedilmiş bir not varsa true — yalnızca o zaman Sil
     *  seçeneği anlamlıdır. */
    private val _isExistingNote = MutableStateFlow(false)
    val isExistingNote = _isExistingNote.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            dailyNoteUseCases.getNoteForDate(dateMillis)?.let { note ->
                _content.value = note.content
                _mood.value = note.mood
                _isExistingNote.value = true
            }
        }
    }

    fun onContentChange(value: String) {
        _content.value = value
    }

    fun onMoodSelected(value: Mood) {
        _mood.value = if (_mood.value == value) null else value
    }

    fun saveNote() {
        viewModelScope.launch {
            if (_content.value.isBlank() && _mood.value == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(appContext.getString(R.string.daily_note_empty_error)))
                return@launch
            }
            dailyNoteUseCases.saveNote(
                DailyNote(dateMillis = dateMillis, content = _content.value, mood = _mood.value)
            )
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            dailyNoteUseCases.deleteNote(DailyNote(dateMillis = dateMillis, content = _content.value, mood = _mood.value))
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveSuccess : UiEvent()
    }
}
