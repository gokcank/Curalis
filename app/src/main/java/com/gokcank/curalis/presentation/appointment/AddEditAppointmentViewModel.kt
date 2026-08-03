package com.gokcank.curalis.presentation.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.usecase.AppointmentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditAppointmentViewModel @Inject constructor(
    private val appointmentUseCases: AppointmentUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()
    
    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()
    
    private val _timeInMillis = MutableStateFlow(System.currentTimeMillis())
    val timeInMillis = _timeInMillis.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentAppointmentId: String? = null
    val isEditMode: Boolean
        get() = currentAppointmentId != null

    init {
        savedStateHandle.get<String>("appointmentId")?.let { appointmentId ->
            if (appointmentId.isNotBlank()) {
                viewModelScope.launch {
                    appointmentUseCases.getAppointmentById(appointmentId).collect { appointment ->
                        appointment?.let {
                            currentAppointmentId = it.id
                            _title.value = it.title
                            _location.value = it.location ?: ""
                            _notes.value = it.notes ?: ""
                            _timeInMillis.value = it.timeInMillis
                        }
                    }
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _title.value = title
    }

    fun onLocationChange(location: String) {
        _location.value = location
    }
    
    fun onNotesChange(notes: String) {
        _notes.value = notes
    }

    fun onTimeChange(time: Long) {
        _timeInMillis.value = time
    }

    fun saveAppointment() {
        viewModelScope.launch {
            if (_title.value.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Randevu başlığı boş olamaz"))
                return@launch
            }

            val appointmentId = currentAppointmentId ?: UUID.randomUUID().toString()
            val appointment = Appointment(
                id = appointmentId,
                title = _title.value,
                location = _location.value.takeIf { it.isNotBlank() },
                notes = _notes.value.takeIf { it.isNotBlank() },
                timeInMillis = _timeInMillis.value,
                doctorId = null // Doctor selection can be added later
            )

            if (currentAppointmentId != null) {
                appointmentUseCases.updateAppointment(appointment)
            } else {
                appointmentUseCases.addAppointment(appointment)
            }
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveSuccess : UiEvent()
    }
}
