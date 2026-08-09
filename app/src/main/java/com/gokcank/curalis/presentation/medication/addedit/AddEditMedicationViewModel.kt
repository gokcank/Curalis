package com.gokcank.curalis.presentation.medication.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.ProviderMedication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import com.gokcank.curalis.domain.usecase.AddMedicationUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationByIdUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersForMedicationUseCase
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    private val getMedicationByIdUseCase: GetMedicationByIdUseCase,
    private val addMedicationUseCase: AddMedicationUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val validateMedicationUseCase: ValidateMedicationUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase,
    private val searchRemoteMedicationsUseCase: SearchRemoteMedicationsUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val stockHistoryRepository: StockHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _medicationName = MutableStateFlow("")
    val medicationName = _medicationName.asStateFlow()

    // Kullanıcı bu ilacı resmi öneri listesinden mi seçti, yoksa elle mi girdi.
    private val _isVerifiedSource = MutableStateFlow(false)
    val isVerifiedSource = _isVerifiedSource.asStateFlow()

    private val _activeIngredient = MutableStateFlow("")
    val activeIngredient = _activeIngredient.asStateFlow()

    private val _formType = MutableStateFlow(MedicationForm.PILL)
    val formType = _formType.asStateFlow()

    private val _medicationDosage = MutableStateFlow("")
    val medicationDosage = _medicationDosage.asStateFlow()

    private val _barcode = MutableStateFlow("")
    val barcode = _barcode.asStateFlow()

    private val _medicationUnit = MutableStateFlow("")
    val medicationUnit = _medicationUnit.asStateFlow()

    private val _mealInstruction = MutableStateFlow(MealInstruction.DOES_NOT_MATTER)
    val mealInstruction = _mealInstruction.asStateFlow()

    private val _colorHex = MutableStateFlow("#1E88E5")
    val colorHex = _colorHex.asStateFlow()

    private val _iconShape = MutableStateFlow("PILL")
    val iconShape = _iconShape.asStateFlow()

    fun onColorSelected(colorHex: String) {
        _colorHex.value = colorHex
    }

    fun onIconShapeSelected(shape: String) {
        _iconShape.value = shape
    }

    private val _medicationNotes = MutableStateFlow("")
    val medicationNotes = _medicationNotes.asStateFlow()

    private val _expiryDate = MutableStateFlow<Long?>(null)
    val expiryDate = _expiryDate.asStateFlow()

    private val _frequencyType = MutableStateFlow(FrequencyType.DAILY)
    val frequencyType = _frequencyType.asStateFlow()

    private val _intervalDays = MutableStateFlow("2")
    val intervalDays = _intervalDays.asStateFlow()

    private val _activeDays = MutableStateFlow("21")
    val activeDays = _activeDays.asStateFlow()

    private val _restDays = MutableStateFlow("7")
    val restDays = _restDays.asStateFlow()

    private val _specificDays = MutableStateFlow<List<Int>>(emptyList())
    val specificDays = _specificDays.asStateFlow()

    private val _isRefillEnabled = MutableStateFlow(false)
    val isRefillEnabled = _isRefillEnabled.asStateFlow()

    private val _currentStock = MutableStateFlow("")
    val currentStock = _currentStock.asStateFlow()

    private val _refillThreshold = MutableStateFlow("5")
    val refillThreshold = _refillThreshold.asStateFlow()

    private val _medicationTimes = MutableStateFlow<List<MedicationTime>>(emptyList())
    val medicationTimes = _medicationTimes.asStateFlow()

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

    private var originalMedication: Medication? = null
    private var searchJob: Job? = null

    private val _stockHistory = MutableStateFlow<List<StockHistoryEntry>>(emptyList())
    val stockHistory = _stockHistory.asStateFlow()

    init {
        savedStateHandle.get<String>("medicationId")?.let { medicationId ->
            if (medicationId.isNotBlank()) {
                viewModelScope.launch {
                    stockHistoryRepository.getHistoryForMedication(medicationId).collect {
                        _stockHistory.value = it
                    }
                }
                viewModelScope.launch {
                    getMedicationByIdUseCase(medicationId).collect { medication ->
                        medication?.let {
                            currentMedicationId = it.id
                            originalMedication = it
                            _medicationName.value = it.name
                            _activeIngredient.value = it.activeIngredient ?: ""
                            _formType.value = it.formType
                            _colorHex.value = it.colorHex
                            _iconShape.value = it.iconShape
                            _medicationDosage.value = it.dosage ?: ""
                            _barcode.value = it.barcode ?: ""
                            _medicationUnit.value = it.unit ?: ""
                            _mealInstruction.value = it.mealInstruction
                            _medicationNotes.value = it.notes ?: ""
                            _expiryDate.value = it.expiryDate
                            _frequencyType.value = it.frequencyType
                            _intervalDays.value = (it.intervalDays ?: 2).toString()
                            _activeDays.value = (it.activeDays ?: 21).toString()
                            _restDays.value = (it.restDays ?: 7).toString()
                            _specificDays.value = it.specificDays
                            _isRefillEnabled.value = it.isRefillReminderEnabled
                            _currentStock.value = it.currentStock?.toString() ?: ""
                            _refillThreshold.value = (it.refillThreshold ?: 5).toString()
                            _isVerifiedSource.value = it.isVerifiedSource
                            _medicationTimes.value = it.times
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _medicationName.value = name
        _errorMessage.value = null
        // Kullanıcı ismi elle değiştiriyor; artık resmi öneriyle birebir eşleştiği garanti değil.
        _isVerifiedSource.value = false

        searchJob?.cancel()
        if (name.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                _isSearching.value = true
                try {
                    _suggestions.value = searchRemoteMedicationsUseCase(name)
                } catch (e: Exception) {
                    _suggestions.value = emptyList()
                } finally {
                    _isSearching.value = false
                }
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun onSuggestionSelected(suggestion: ProviderMedication) {
        _medicationName.value = suggestion.name
        _activeIngredient.value = suggestion.activeIngredient ?: ""
        if (!suggestion.dosage.isNullOrBlank()) {
            _medicationDosage.value = suggestion.dosage
        }
        _isVerifiedSource.value = true
        _suggestions.value = emptyList()
    }

    fun onActiveIngredientChange(value: String) {
        _activeIngredient.value = value
    }

    fun onFormTypeChange(form: MedicationForm) {
        _formType.value = form
    }

    fun onDosageChange(dosage: String) {
        _medicationDosage.value = dosage
    }

    fun onBarcodeChange(code: String) {
        _barcode.value = code
    }

    fun onUnitChange(unit: String) {
        _medicationUnit.value = unit
    }

    fun onMealInstructionChange(instruction: MealInstruction) {
        _mealInstruction.value = instruction
    }

    fun onNotesChange(notes: String) {
        _medicationNotes.value = notes
    }

    fun onExpiryDateChange(timestamp: Long?) {
        _expiryDate.value = timestamp
    }

    fun onFrequencyTypeChange(type: FrequencyType) {
        _frequencyType.value = type
    }

    fun onIntervalDaysChange(days: String) {
        _intervalDays.value = days.filter { it.isDigit() }
    }

    fun onActiveDaysChange(days: String) {
        _activeDays.value = days.filter { it.isDigit() }
    }

    fun onRestDaysChange(days: String) {
        _restDays.value = days.filter { it.isDigit() }
    }

    fun toggleSpecificDay(day: Int) {
        val current = _specificDays.value.toMutableList()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _specificDays.value = current.sorted()
    }

    fun onRefillToggle(enabled: Boolean) {
        _isRefillEnabled.value = enabled
    }

    fun onCurrentStockChange(stock: String) {
        _currentStock.value = stock.filter { it.isDigit() }
    }

    fun onRefillThresholdChange(threshold: String) {
        _refillThreshold.value = threshold.filter { it.isDigit() }
    }

    fun addMedicationTime(hour: Int, minute: Int, dose: String? = null) {
        val newTime = MedicationTime(
            id = UUID.randomUUID().toString(),
            hour = hour,
            minute = minute,
            dose = dose?.takeIf { it.isNotBlank() }
        )
        _medicationTimes.value = (_medicationTimes.value + newTime).sortedWith(compareBy({ it.hour }, { it.minute }))
    }

    fun removeMedicationTime(timeId: String) {
        _medicationTimes.value = _medicationTimes.value.filterNot { it.id == timeId }
    }

    fun saveMedication() {
        viewModelScope.launch {
            if (!validateMedicationUseCase(_medicationName.value)) {
                _errorMessage.value = "İlaç adı boş bırakılamaz."
                return@launch
            }

            try {
                val medicationId = currentMedicationId ?: UUID.randomUUID().toString()
                val parsedStock = _currentStock.value.toIntOrNull()
                val parsedThreshold = _refillThreshold.value.toIntOrNull() ?: 5
                val parsedInterval = _intervalDays.value.toIntOrNull() ?: 2
                val parsedActiveDays = _activeDays.value.toIntOrNull() ?: 21
                val parsedRestDays = _restDays.value.toIntOrNull() ?: 7

                val medication = Medication(
                    id = medicationId,
                    name = _medicationName.value,
                    barcode = _barcode.value.takeIf { it.isNotBlank() },
                    activeIngredient = _activeIngredient.value.takeIf { it.isNotBlank() },
                    form = _formType.value.displayNameTr,
                    formType = _formType.value,
                    colorHex = _colorHex.value,
                    iconShape = _iconShape.value,
                    dosage = _medicationDosage.value.takeIf { it.isNotBlank() },
                    unit = _medicationUnit.value.takeIf { it.isNotBlank() },
                    notes = _medicationNotes.value.takeIf { it.isNotBlank() },
                    mealInstruction = _mealInstruction.value,
                    expiryDate = _expiryDate.value,
                    frequencyType = _frequencyType.value,
                    intervalDays = if (_frequencyType.value == FrequencyType.INTERVAL) parsedInterval else null,
                    specificDays = if (_frequencyType.value == FrequencyType.SPECIFIC_DAYS) _specificDays.value else emptyList(),
                    activeDays = if (_frequencyType.value == FrequencyType.CYCLIC) parsedActiveDays else null,
                    restDays = if (_frequencyType.value == FrequencyType.CYCLIC) parsedRestDays else null,
                    initialStock = parsedStock,
                    currentStock = parsedStock,
                    refillThreshold = parsedThreshold,
                    isRefillReminderEnabled = _isRefillEnabled.value,
                    isVerifiedSource = _isVerifiedSource.value,
                    times = _medicationTimes.value
                )

                // Bu ilaca ait önceden kurulmuş tüm alarmları iptal et (düzenlemede
                // eski saatler/eski kayıtlar sistemde asılı kalmasın diye).
                getRemindersForMedicationUseCase(medicationId).firstOrNull()?.forEach { existingReminder ->
                    alarmScheduler.cancel(existingReminder.id)
                }
                originalMedication?.let { old -> alarmScheduler.cancelMedicationAlarms(old) }

                if (currentMedicationId != null) {
                    // Kullanıcı stoğu elle değiştirdiyse (yeni kutu girdi, düzeltme yaptı vb.)
                    // bunu da geçmişe kaydet — daha önce stok her düzenlemede sessizce
                    // üzerine yazılıyordu.
                    val oldStock = originalMedication?.currentStock
                    if (oldStock != parsedStock) {
                        stockHistoryRepository.logChange(
                            StockHistoryEntry(
                                medicationId = medicationId,
                                previousStock = oldStock,
                                newStock = parsedStock,
                                reason = if (parsedStock != null && oldStock != null && parsedStock > oldStock) {
                                    StockChangeReason.REFILL
                                } else {
                                    StockChangeReason.MANUAL_EDIT
                                }
                            )
                        )
                    }
                    updateMedicationUseCase(medication)
                } else {
                    addMedicationUseCase(medication)
                }

                // Schedule alarms for all configured times
                _medicationTimes.value.forEach { medTime ->
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, medTime.hour)
                        set(Calendar.MINUTE, medTime.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }

                    val reminder = Reminder(
                        medicationId = medicationId,
                        timeInMillis = calendar.timeInMillis
                    )
                    scheduleReminderUseCase(reminder)
                    alarmScheduler.schedule(reminder, medication.name)
                }

                if (!alarmScheduler.canScheduleExactAlarms()) {
                    _errorMessage.value = "İlaç kaydedildi ancak tam zamanlı alarm izni verilmediği için hatırlatıcılar tetiklenmeyebilir. Lütfen sistem ayarlarından izni açın."
                }

                _eventFlow.emit(UiEvent.SaveSuccess)
            } catch (e: Exception) {
                _errorMessage.value = "İlaç kaydedilirken bir hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}"
            }
        }
    }

    sealed class UiEvent {
        data object SaveSuccess : UiEvent()
    }
}
