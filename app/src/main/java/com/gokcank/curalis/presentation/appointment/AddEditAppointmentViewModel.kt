package com.gokcank.curalis.presentation.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.model.EmergencyContact
import com.gokcank.curalis.domain.usecase.AppointmentUseCases
import com.gokcank.curalis.domain.usecase.DoctorUseCases
import com.gokcank.curalis.domain.usecase.EmergencyContactUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditAppointmentViewModel @Inject constructor(
    private val appointmentUseCases: AppointmentUseCases,
    private val doctorUseCases: DoctorUseCases,
    private val emergencyContactUseCases: EmergencyContactUseCases,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val doctors: kotlinx.coroutines.flow.StateFlow<List<Doctor>> = doctorUseCases.getDoctors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: kotlinx.coroutines.flow.StateFlow<List<EmergencyContact>> = emergencyContactUseCases.getEmergencyContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _timeInMillis = MutableStateFlow(System.currentTimeMillis())
    val timeInMillis = _timeInMillis.asStateFlow()

    private val _selectedDoctorId = MutableStateFlow<String?>(null)
    val selectedDoctorId = _selectedDoctorId.asStateFlow()

    private val _selectedEmergencyContactId = MutableStateFlow<String?>(null)
    val selectedEmergencyContactId = _selectedEmergencyContactId.asStateFlow()

    private val _isVisited = MutableStateFlow(false)
    val isVisited = _isVisited.asStateFlow()

    private val _visitNote = MutableStateFlow("")
    val visitNote = _visitNote.asStateFlow()

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
                            _selectedDoctorId.value = it.doctorId
                            _selectedEmergencyContactId.value = it.emergencyContactId
                            _isVisited.value = it.isVisited
                            _visitNote.value = it.visitNote ?: ""
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

    fun onDoctorSelected(doctorId: String?) {
        _selectedDoctorId.value = doctorId
        // Randevu Doktor'un ya da Acil Durum Kişisi'nin yalnızca birine atanabilir.
        if (doctorId != null) _selectedEmergencyContactId.value = null
    }

    fun onEmergencyContactSelected(contactId: String?) {
        _selectedEmergencyContactId.value = contactId
        if (contactId != null) _selectedDoctorId.value = null
    }

    fun onVisitedChange(visited: Boolean) {
        _isVisited.value = visited
    }

    fun onVisitNoteChange(note: String) {
        _visitNote.value = note
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
                doctorId = _selectedDoctorId.value,
                emergencyContactId = _selectedEmergencyContactId.value,
                isVisited = _isVisited.value,
                visitNote = _visitNote.value.takeIf { it.isNotBlank() }
            )

            if (currentAppointmentId != null) {
                appointmentUseCases.updateAppointment(appointment)
            } else {
                appointmentUseCases.addAppointment(appointment)
            }

            // Schedule appointment notification
            alarmScheduler.scheduleAppointmentReminder(appointment)

            // Ekran, SaveSuccess'te hemen geri dönüyor; izin eksikse kullanıcıyı
            // ayarlara yönlendirme fırsatını kaçırmamak için geri dönmeden önce
            // ayrı bir olayla bildiriyoruz (bkz. AddEditMedicationViewModel).
            if (!alarmScheduler.canScheduleExactAlarms()) {
                _eventFlow.emit(UiEvent.ExactAlarmPermissionMissing)
            } else {
                _eventFlow.emit(UiEvent.SaveSuccess)
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveSuccess : UiEvent()
        data object ExactAlarmPermissionMissing : UiEvent()
    }
}
