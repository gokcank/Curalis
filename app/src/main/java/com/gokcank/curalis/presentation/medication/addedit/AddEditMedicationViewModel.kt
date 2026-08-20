package com.gokcank.curalis.presentation.medication.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.core.utils.MedicationPhotoStorage
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.usecase.DoctorUseCases
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.ProviderMedication
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import com.gokcank.curalis.domain.usecase.AddMedicationUseCase
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationByIdUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersForMedicationUseCase
import com.gokcank.curalis.domain.usecase.SearchRemoteMedicationsUseCase
import com.gokcank.curalis.domain.usecase.UpdateMedicationUseCase
import com.gokcank.curalis.domain.usecase.ValidateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * İlaç ekleme/düzenleme formunun tüm kullanıcı tarafından girilebilir alanları.
 *
 * Daha önce bu alanların her biri ayrı bir MutableStateFlow'du; yeni bir alan eklemek
 * dört ayrı yere (backing flow, public flow, yükleme, kaydetme) dokunmayı gerektiriyor ve
 * birini atlayınca alan sessizce kaybolabiliyordu. Tek bir state nesnesi + simetrik
 * [toFormState]/[toMedication] dönüşümleri bu riski ortadan kaldırıyor: eksik bir alan
 * iki dönüşüm fonksiyonu yan yana okunduğunda hemen göze çarpar.
 *
 * Not: Arama sonuçları, yükleniyor durumu, hata mesajı ve stok geçmişi buraya dahil
 * DEĞİLDİR — bunlar formun içeriği değil, geçici/harici durumlardır ve ayrı akışlarda tutulur.
 */
data class MedicationFormState(
    val name: String = "",
    /** Kullanıcı bu ilacı resmi öneri listesinden mi seçti, yoksa elle mi girdi. */
    val isVerifiedSource: Boolean = false,
    val activeIngredient: String = "",
    val formType: MedicationForm = MedicationForm.PILL,
    val dosage: String = "",
    val barcode: String = "",
    val unit: String = "",
    val mealInstruction: MealInstruction = MealInstruction.DOES_NOT_MATTER,
    val colorHex: String = "#1E88E5",
    val iconShape: String = "PILL",
    val notes: String = "",
    val expiryDate: Long? = null,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val intervalDays: String = "2",
    val activeDays: String = "21",
    val restDays: String = "7",
    val hasPlaceboDays: Boolean = false,
    val specificDays: List<Int> = emptyList(),
    val isRefillEnabled: Boolean = false,
    val currentStock: String = "",
    val refillThreshold: String = "5",
    val photoPath: String? = null,
    val doctorId: String? = null,
    val times: List<MedicationTime> = emptyList(),
    val treatmentDurationDays: String = "",
    val rxNumber: String = ""
)

/** Kayıtlı bir ilacı forma yükler. [MedicationFormState.toMedication] ile simetriktir. */
private fun Medication.toFormState(): MedicationFormState = MedicationFormState(
    name = name,
    isVerifiedSource = isVerifiedSource,
    activeIngredient = activeIngredient ?: "",
    formType = formType,
    dosage = dosage ?: "",
    barcode = barcode ?: "",
    unit = unit ?: "",
    mealInstruction = mealInstruction,
    colorHex = colorHex,
    iconShape = iconShape,
    notes = notes ?: "",
    expiryDate = expiryDate,
    frequencyType = frequencyType,
    intervalDays = (intervalDays ?: 2).toString(),
    activeDays = (activeDays ?: 21).toString(),
    restDays = (restDays ?: 7).toString(),
    hasPlaceboDays = hasPlaceboDays,
    specificDays = specificDays,
    isRefillEnabled = isRefillReminderEnabled,
    currentStock = currentStock?.toString() ?: "",
    refillThreshold = (refillThreshold ?: 5).toString(),
    photoPath = photoPath,
    doctorId = doctorId,
    times = times,
    treatmentDurationDays = treatmentDurationDays?.toString() ?: "",
    rxNumber = rxNumber ?: ""
)

/** Formu kaydedilebilir bir ilaca çevirir. [Medication.toFormState] ile simetriktir. */
private fun MedicationFormState.toMedication(id: String): Medication {
    val parsedStock = currentStock.toIntOrNull()
    return Medication(
        id = id,
        name = name,
        barcode = barcode.takeIf { it.isNotBlank() },
        activeIngredient = activeIngredient.takeIf { it.isNotBlank() },
        form = formType.displayNameTr,
        formType = formType,
        colorHex = colorHex,
        iconShape = iconShape,
        dosage = dosage.takeIf { it.isNotBlank() },
        unit = unit.takeIf { it.isNotBlank() },
        notes = notes.takeIf { it.isNotBlank() },
        mealInstruction = mealInstruction,
        expiryDate = expiryDate,
        frequencyType = frequencyType,
        intervalDays = if (frequencyType == FrequencyType.INTERVAL) intervalDays.toIntOrNull() ?: 2 else null,
        specificDays = if (frequencyType == FrequencyType.SPECIFIC_DAYS) specificDays else emptyList(),
        activeDays = if (frequencyType == FrequencyType.CYCLIC) activeDays.toIntOrNull() ?: 21 else null,
        restDays = if (frequencyType == FrequencyType.CYCLIC) restDays.toIntOrNull() ?: 7 else null,
        hasPlaceboDays = frequencyType == FrequencyType.CYCLIC && hasPlaceboDays,
        initialStock = parsedStock,
        currentStock = parsedStock,
        refillThreshold = refillThreshold.toIntOrNull() ?: 5,
        isRefillReminderEnabled = isRefillEnabled,
        isVerifiedSource = isVerifiedSource,
        treatmentDurationDays = treatmentDurationDays.toIntOrNull(),
        rxNumber = rxNumber.takeIf { it.isNotBlank() },
        photoPath = photoPath,
        doctorId = doctorId,
        times = times
    )
}

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    private val getMedicationByIdUseCase: GetMedicationByIdUseCase,
    private val addMedicationUseCase: AddMedicationUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val validateMedicationUseCase: ValidateMedicationUseCase,
    private val generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase,
    private val getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase,
    private val searchRemoteMedicationsUseCase: SearchRemoteMedicationsUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val reminderRepository: ReminderRepository,
    private val stockHistoryRepository: StockHistoryRepository,
    private val photoStorage: MedicationPhotoStorage,
    doctorUseCases: DoctorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(MedicationFormState())
    val formState = _formState.asStateFlow()

    val doctors: StateFlow<List<Doctor>> = doctorUseCases.getDoctors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Formun içeriği değil, geçici/harici durumlar — bilinçli olarak MedicationFormState dışında.
    private val _suggestions = MutableStateFlow<List<ProviderMedication>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _stockHistory = MutableStateFlow<List<StockHistoryEntry>>(emptyList())
    val stockHistory = _stockHistory.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentMedicationId: String? = null
    val isEditMode: Boolean
        get() = currentMedicationId != null

    private var originalMedication: Medication? = null
    private var searchJob: Job? = null

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
                            _formState.value = it.toFormState()
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        // Kullanıcı ismi elle değiştiriyor; artık resmi öneriyle birebir eşleştiği garanti değil.
        _formState.update { it.copy(name = name, isVerifiedSource = false) }
        _errorMessage.value = null

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
        _formState.update {
            it.copy(
                name = suggestion.name,
                activeIngredient = suggestion.activeIngredient ?: "",
                // Öneride doz yoksa kullanıcının elle girdiği dozu ezme.
                dosage = suggestion.dosage?.takeIf { d -> d.isNotBlank() } ?: it.dosage,
                isVerifiedSource = true
            )
        }
        _suggestions.value = emptyList()
    }

    fun onActiveIngredientChange(value: String) = _formState.update { it.copy(activeIngredient = value) }

    fun onFormTypeChange(form: MedicationForm) = _formState.update { it.copy(formType = form) }

    fun onDosageChange(dosage: String) = _formState.update { it.copy(dosage = dosage) }

    fun onBarcodeChange(code: String) = _formState.update { it.copy(barcode = code) }

    fun onUnitChange(unit: String) = _formState.update { it.copy(unit = unit) }

    fun onMealInstructionChange(instruction: MealInstruction) = _formState.update { it.copy(mealInstruction = instruction) }

    fun onColorSelected(colorHex: String) = _formState.update { it.copy(colorHex = colorHex) }

    fun onIconShapeSelected(shape: String) = _formState.update { it.copy(iconShape = shape) }

    fun onNotesChange(notes: String) = _formState.update { it.copy(notes = notes) }

    fun onExpiryDateChange(timestamp: Long?) = _formState.update { it.copy(expiryDate = timestamp) }

    fun onTreatmentDurationChange(days: String) = _formState.update { it.copy(treatmentDurationDays = days) }

    fun onRxNumberChange(value: String) = _formState.update { it.copy(rxNumber = value) }

    fun onFrequencyTypeChange(type: FrequencyType) = _formState.update { it.copy(frequencyType = type) }

    fun onIntervalDaysChange(days: String) = _formState.update { it.copy(intervalDays = days.filter { c -> c.isDigit() }) }

    fun onActiveDaysChange(days: String) = _formState.update { it.copy(activeDays = days.filter { c -> c.isDigit() }) }

    fun onRestDaysChange(days: String) = _formState.update { it.copy(restDays = days.filter { c -> c.isDigit() }) }

    fun onHasPlaceboDaysChange(enabled: Boolean) = _formState.update { it.copy(hasPlaceboDays = enabled) }

    fun toggleSpecificDay(day: Int) = _formState.update { state ->
        val updated = if (state.specificDays.contains(day)) {
            state.specificDays - day
        } else {
            state.specificDays + day
        }
        state.copy(specificDays = updated.sorted())
    }

    fun onRefillToggle(enabled: Boolean) = _formState.update { it.copy(isRefillEnabled = enabled) }

    fun onCurrentStockChange(stock: String) = _formState.update { it.copy(currentStock = stock.filter { c -> c.isDigit() }) }

    fun onRefillThresholdChange(threshold: String) = _formState.update { it.copy(refillThreshold = threshold.filter { c -> c.isDigit() }) }

    fun onDoctorSelected(doctorId: String?) = _formState.update { it.copy(doctorId = doctorId) }

    /** Kamera uygulamasının fotoğrafı yazacağı bir hedef hazırlar; sonucu [onPhotoCaptured] alır. */
    fun prepareCaptureTarget() = photoStorage.createCaptureTarget()

    fun onPhotoCaptured(file: java.io.File) {
        val previous = _formState.value.photoPath
        _formState.update { it.copy(photoPath = file.absolutePath) }
        deleteIfUnsavedReplacement(previous, file.absolutePath)
    }

    fun onGalleryPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val previous = _formState.value.photoPath
            val newPath = photoStorage.copyFromUri(uri) ?: return@launch
            _formState.update { it.copy(photoPath = newPath) }
            deleteIfUnsavedReplacement(previous, newPath)
        }
    }

    fun onRemovePhoto() {
        val previous = _formState.value.photoPath
        _formState.update { it.copy(photoPath = null) }
        deleteIfUnsavedReplacement(previous, null)
    }

    /**
     * [previous] dosyasını yalnızca hiçbir zaman kaydedilmemişse (bu düzenleme oturumunda
     * yeni çekilmiş/seçilmiş ama henüz "Kaydet"e basılmamış bir dosyaysa) hemen siler.
     * Aksi halde — yani [previous] veritabanındaki orijinal ilaç kaydına aitse — kullanıcı
     * kaydetmeden vazgeçebilir; o durumda dosyayı hemen silmek DB'de kırık bir referans
     * bırakırdı. Orijinal dosyanın temizliği [saveMedication] içinde, değişiklik gerçekten
     * kaydedildikten sonra yapılır.
     */
    private fun deleteIfUnsavedReplacement(previous: String?, newPath: String?) {
        if (previous == null || previous == newPath) return
        if (previous == originalMedication?.photoPath) return
        photoStorage.delete(previous)
    }

    fun addMedicationTime(hour: Int, minute: Int, dose: String? = null) {
        val newTime = MedicationTime(
            id = UUID.randomUUID().toString(),
            hour = hour,
            minute = minute,
            dose = dose?.takeIf { it.isNotBlank() }
        )
        _formState.update { state ->
            state.copy(times = (state.times + newTime).sortedWith(compareBy({ it.hour }, { it.minute })))
        }
    }

    fun removeMedicationTime(timeId: String) = _formState.update { state ->
        state.copy(times = state.times.filterNot { it.id == timeId })
    }

    /** saveMedication() saatleri değiştiyse kullanıcıya kapsam sorana kadar burada bekletilir. */
    private var pendingScheduleChangeMedication: Medication? = null

    fun saveMedication() {
        viewModelScope.launch {
            // Kaydetme boyunca tek bir anlık görüntü üzerinden çalışılır; alan alan
            // .value okunduğunda kullanıcı yazmaya devam ederse tutarsız bir kayıt oluşabilirdi.
            val form = _formState.value

            if (!validateMedicationUseCase(form.name)) {
                _errorMessage.value = "İlaç adı boş bırakılamaz."
                return@launch
            }

            val medicationId = currentMedicationId ?: UUID.randomUUID().toString()
            // isArchived/isSuspended form alanları değildir (Askıya Al/Arşivle işlemleri
            // İlaçlarım listesinden yapılır); form burada bunları sıfırlarsa, askıya
            // alınmış bir ilacı düzenleyip kaydetmek sessizce "devam ediyor" durumuna
            // döndürürdü.
            val medication = form.toMedication(medicationId).let { med ->
                originalMedication?.let { med.copy(isArchived = it.isArchived, isSuspended = it.isSuspended) } ?: med
            }

            // Saatler değiştiyse (eklendi/çıkarıldı/saati değişti) ve bekleyen gelecek
            // dozlar varsa, kullanıcıya bu değişikliğin kapsamını sor — aksi halde eski
            // saatteki bekleyen kayıtlar hiç temizlenmeden sistemde asılı kalıyordu.
            val original = originalMedication
            if (original != null && original.times != medication.times) {
                val hasPendingFutureReminders = reminderRepository.getRemindersForMedication(medicationId)
                    .firstOrNull()
                    ?.any {
                        (it.state == ReminderState.SCHEDULED || it.state == ReminderState.DELIVERED) &&
                            it.timeInMillis > System.currentTimeMillis()
                    } ?: false

                if (hasPendingFutureReminders) {
                    pendingScheduleChangeMedication = medication
                    _eventFlow.emit(UiEvent.ConfirmScheduleChangeScope)
                    return@launch
                }
            }

            persistMedication(medication, ScheduleChangeScope.FROM_NOW)
        }
    }

    /** Kullanıcı saat değişikliğinin kapsamını seçtikten sonra kaydı tamamlar. */
    fun confirmScheduleChangeScope(scope: ScheduleChangeScope) {
        val medication = pendingScheduleChangeMedication ?: return
        pendingScheduleChangeMedication = null
        viewModelScope.launch {
            persistMedication(medication, scope)
        }
    }

    private suspend fun persistMedication(medication: Medication, scope: ScheduleChangeScope) {
        val medicationId = medication.id
        try {
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
                val parsedStock = medication.currentStock
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
                // Fotoğraf değiştirildi/kaldırıldıysa, artık hiçbir yerden referans
                // verilmeyen eski dosya burada, değişiklik kalıcı olarak kaydedildikten
                // sonra temizlenir.
                val oldPhotoPath = originalMedication?.photoPath
                if (oldPhotoPath != null && oldPhotoPath != medication.photoPath) {
                    photoStorage.delete(oldPhotoPath)
                }
            } else {
                addMedicationUseCase(medication)
            }

            // scope == FROM_TOMORROW ise bugünün zaten planlanmış dozlarına dokunulmaz;
            // yeni saatler yalnızca yarından itibaren geçerli olur. FROM_NOW ise (yeni
            // ilaç ya da bekleyen gelecek dozu olmayan bir düzenleme) her zamanki gibi
            // şimdiden itibaren uygulanır.
            val regenerateFromMillis = when (scope) {
                ScheduleChangeScope.FROM_NOW -> System.currentTimeMillis()
                ScheduleChangeScope.FROM_TOMORROW -> startOfTomorrowMillis() - 1
            }

            // Eski saatlere ait, artık geçerli olmayan bekleyen kayıtları kapsam
            // dahilinde temizle; generateUpcomingRemindersUseCase var olan kayıtların
            // üzerine yazmadığı için bu adım olmadan eski saatteki "hayalet" dozlar
            // sistemde asılı kalırdı.
            reminderRepository.getRemindersForMedication(medicationId).firstOrNull()
                ?.filter {
                    (it.state == ReminderState.SCHEDULED || it.state == ReminderState.DELIVERED) &&
                        it.timeInMillis >= regenerateFromMillis
                }
                ?.forEach { reminderRepository.deleteReminder(it) }

            // Önündeki 30 günün tüm dozlarını gerçek kayıtlar olarak veritabanına yaz
            // (Ana Sayfa/rapor/Günlük Program artık aynı kalıcı kayıtları görsün diye),
            // ardından bir sonraki tetiklenme için native alarmı kur.
            generateUpcomingRemindersUseCase(medication, fromMillis = regenerateFromMillis)
            alarmScheduler.scheduleMedicationAlarms(medication)

            // Ekran, SaveSuccess'te hemen geri dönüyor (bkz. AddEditMedicationScreen);
            // izin eksikse burada gösterilen bir metin kullanıcıya hiç ulaşmadan
            // ekrandan çıkılırdı. Bu yüzden geri dönmeden önce ayrı bir olayla
            // kullanıcıyı ayarlara yönlendirme fırsatı veriyoruz.
            if (!alarmScheduler.canScheduleExactAlarms()) {
                _eventFlow.emit(UiEvent.ExactAlarmPermissionMissing)
            } else {
                _eventFlow.emit(UiEvent.SaveSuccess)
            }
        } catch (e: Exception) {
            _errorMessage.value = "İlaç kaydedilirken bir hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}"
        }
    }

    private fun startOfTomorrowMillis(): Long {
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    enum class ScheduleChangeScope { FROM_NOW, FROM_TOMORROW }

    sealed class UiEvent {
        data object SaveSuccess : UiEvent()
        data object ExactAlarmPermissionMissing : UiEvent()
        data object ConfirmScheduleChangeScope : UiEvent()
    }
}
