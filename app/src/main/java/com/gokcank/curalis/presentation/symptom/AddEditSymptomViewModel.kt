package com.gokcank.curalis.presentation.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Symptom
import com.gokcank.curalis.domain.model.SymptomType
import com.gokcank.curalis.domain.usecase.SymptomUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditSymptomViewModel @Inject constructor(
    private val symptomUseCases: SymptomUseCases
) : ViewModel() {

    private val _selectedType = MutableStateFlow(SymptomType.PAIN)
    val selectedType = _selectedType.asStateFlow()

    private val _severity = MutableStateFlow(5f)
    val severity = _severity.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onTypeSelected(type: SymptomType) {
        _selectedType.value = type
    }

    fun onSeverityChange(value: Float) {
        _severity.value = value
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun saveSymptom() {
        viewModelScope.launch {
            val symptom = Symptom(
                type = _selectedType.value,
                severity = _severity.value.toInt(),
                notes = _notes.value.takeIf { it.isNotBlank() }
            )
            symptomUseCases.addSymptom(symptom)
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data object SaveSuccess : UiEvent()
    }
}
