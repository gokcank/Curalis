package com.gokcank.curalis.presentation.medication.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.usecase.AddMedicationUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationByIdUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
import com.gokcank.curalis.domain.usecase.UpdateMedicationUseCase
import com.gokcank.curalis.domain.usecase.ValidateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    private val getMedicationByIdUseCase: GetMedicationByIdUseCase,
    private val addMedicationUseCase: AddMedicationUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val validateMedicationUseCase: ValidateMedicationUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _medicationName = MutableStateFlow("")
    val medicationName = _medicationName.asStateFlow()

    private val _medicationDosage = MutableStateFlow("")
    val medicationDosage = _medicationDosage.asStateFlow()

    private val _medicationUnit = MutableStateFlow("")
    val medicationUnit = _medicationUnit.asStateFlow()

    private val _reminderMinutes = MutableStateFlow<String>("") // Minute offset for testing/reminder
    val reminderMinutes = _reminderMinutes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentMedicationId: String? = null

    init {
        savedStateHandle.get<String>("medicationId")?.let { medicationId ->
            if (medicationId.isNotBlank()) {
                viewModelScope.launch {
                    getMedicationByIdUseCase(medicationId).collect { medication ->
                        medication?.let {
                            currentMedicationId = it.id
                            _medicationName.value = it.name
                            _medicationDosage.value = it.dosage ?: ""
                            _medicationUnit.value = it.unit ?: ""
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _medicationName.value = name
        _errorMessage.value = null
    }

    fun onDosageChange(dosage: String) {
        _medicationDosage.value = dosage
    }

    fun onUnitChange(unit: String) {
        _medicationUnit.value = unit
    }

    fun onReminderMinutesChange(minutes: String) {
        _reminderMinutes.value = minutes
    }

    fun saveMedication() {
        viewModelScope.launch {
            if (!validateMedicationUseCase(_medicationName.value)) {
                _errorMessage.value = "Name cannot be empty."
                return@launch
            }
            
            val medicationId = currentMedicationId ?: java.util.UUID.randomUUID().toString()
            val medication = Medication(
                id = medicationId,
                name = _medicationName.value,
                dosage = _medicationDosage.value.takeIf { it.isNotBlank() },
                unit = _medicationUnit.value.takeIf { it.isNotBlank() }
            )

            if (currentMedicationId != null) {
                updateMedicationUseCase(medication)
            } else {
                addMedicationUseCase(medication)
            }

            // Schedule reminder if reminderMinutes is specified
            val mins = _reminderMinutes.value.toLongOrNull()
            if (mins != null && mins > 0) {
                val triggerTime = System.currentTimeMillis() + (mins * 60 * 1000)
                val reminder = Reminder(
                    medicationId = medicationId,
                    timeInMillis = triggerTime
                )
                scheduleReminderUseCase(reminder)
                alarmScheduler.schedule(reminder, medication.name)
            }

            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data object SaveSuccess : UiEvent()
    }
}
