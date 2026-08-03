package com.gokcank.curalis.presentation.medication.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.ProviderMedication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.usecase.AddMedicationUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationByIdUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
import com.gokcank.curalis.domain.usecase.SearchRemoteMedicationsUseCase
import com.gokcank.curalis.domain.usecase.UpdateMedicationUseCase
import com.gokcank.curalis.domain.usecase.ValidateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    private val getMedicationByIdUseCase: GetMedicationByIdUseCase,
    private val addMedicationUseCase: AddMedicationUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val validateMedicationUseCase: ValidateMedicationUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val searchRemoteMedicationsUseCase: SearchRemoteMedicationsUseCase,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _medicationName = MutableStateFlow("")
    val medicationName = _medicationName.asStateFlow()

    private val _activeIngredient = MutableStateFlow("")
    val activeIngredient = _activeIngredient.asStateFlow()

    private val _form = MutableStateFlow("")
    val form = _form.asStateFlow()

    private val _medicationDosage = MutableStateFlow("")
    val medicationDosage = _medicationDosage.asStateFlow()

    private val _medicationUnit = MutableStateFlow("")
    val medicationUnit = _medicationUnit.asStateFlow()

    private val _reminderTime = MutableStateFlow<Pair<Int, Int>?>(null) // Hour, Minute
    val reminderTime = _reminderTime.asStateFlow()

    private val _suggestions = MutableStateFlow<List<ProviderMedication>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentMedicationId: String? = null
    val isEditMode: Boolean
        get() = currentMedicationId != null

    private var searchJob: Job? = null

    init {
        savedStateHandle.get<String>("medicationId")?.let { medicationId ->
            if (medicationId.isNotBlank()) {
                viewModelScope.launch {
                    getMedicationByIdUseCase(medicationId).collect { medication ->
                        medication?.let {
                            currentMedicationId = it.id
                            _medicationName.value = it.name
                            _activeIngredient.value = it.activeIngredient ?: ""
                            _form.value = it.form ?: ""
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

        searchJob?.cancel()
        if (name.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce
                _isSearching.value = true
                _suggestions.value = searchRemoteMedicationsUseCase(name)
                _isSearching.value = false
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun onSuggestionSelected(suggestion: ProviderMedication) {
        _medicationName.value = suggestion.name
        _activeIngredient.value = suggestion.activeIngredient ?: ""
        _form.value = suggestion.form ?: ""
        if (!suggestion.dosage.isNullOrBlank()) {
            _medicationDosage.value = suggestion.dosage
        }
        _suggestions.value = emptyList()
    }

    fun onActiveIngredientChange(value: String) {
        _activeIngredient.value = value
    }

    fun onFormChange(value: String) {
        _form.value = value
    }

    fun onDosageChange(dosage: String) {
        _medicationDosage.value = dosage
    }

    fun onUnitChange(unit: String) {
        _medicationUnit.value = unit
    }

    fun onReminderTimeSelected(hour: Int, minute: Int) {
        _reminderTime.value = Pair(hour, minute)
    }

    fun clearReminderTime() {
        _reminderTime.value = null
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
                activeIngredient = _activeIngredient.value.takeIf { it.isNotBlank() },
                form = _form.value.takeIf { it.isNotBlank() },
                dosage = _medicationDosage.value.takeIf { it.isNotBlank() },
                unit = _medicationUnit.value.takeIf { it.isNotBlank() }
            )

            if (currentMedicationId != null) {
                updateMedicationUseCase(medication)
            } else {
                addMedicationUseCase(medication)
            }

            // Schedule reminder if time is selected
            _reminderTime.value?.let { (hour, minute) ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1) // If time has passed today, schedule for tomorrow
                    }
                }

                val reminder = Reminder(
                    medicationId = medicationId,
                    timeInMillis = calendar.timeInMillis
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
