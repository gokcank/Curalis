package com.gokcank.curalis.presentation.vital

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.usecase.VitalUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditVitalViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val vitalUseCases: VitalUseCases
) : ViewModel() {

    private val _selectedType = MutableStateFlow(VitalType.BLOOD_PRESSURE)
    val selectedType = _selectedType.asStateFlow()

    private val _value1 = MutableStateFlow("")
    val value1 = _value1.asStateFlow()

    private val _value2 = MutableStateFlow("")
    val value2 = _value2.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    /** Yalnızca VitalType.WEIGHT için anlamlıdır; diğer türlerde selectedType.defaultUnit kullanılır. */
    private val _weightUnit = MutableStateFlow("kg")
    val weightUnit = _weightUnit.asStateFlow()

    fun onWeightUnitSelected(unit: String) {
        _weightUnit.value = unit
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onTypeSelected(type: VitalType) {
        _selectedType.value = type
        // Reset values when type changes
        _value1.value = ""
        _value2.value = ""
    }

    fun onValue1Change(value: String) {
        _value1.value = value
    }

    fun onValue2Change(value: String) {
        _value2.value = value
    }

    fun onNotesChange(notes: String) {
        _notes.value = notes
    }

    fun saveVital() {
        viewModelScope.launch {
            val v1 = _value1.value.toDoubleOrNull()
            if (v1 == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(appContext.getString(R.string.vital_invalid_value_error)))
                return@launch
            }

            val v2 = _value2.value.toDoubleOrNull()

            val vital = Vital(
                id = UUID.randomUUID().toString(),
                type = _selectedType.value,
                value1 = v1,
                value2 = v2,
                unit = if (_selectedType.value == VitalType.WEIGHT) _weightUnit.value else _selectedType.value.defaultUnit,
                timeInMillis = System.currentTimeMillis(),
                notes = _notes.value.takeIf { it.isNotBlank() }
            )

            vitalUseCases.addVital(vital)
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveSuccess : UiEvent()
    }
}
