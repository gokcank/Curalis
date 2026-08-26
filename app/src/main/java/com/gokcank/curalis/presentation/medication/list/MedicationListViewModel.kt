package com.gokcank.curalis.presentation.medication.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.usecase.ArchiveMedicationUseCase
import com.gokcank.curalis.domain.usecase.DeleteMedicationUseCase
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersForMedicationUseCase
import com.gokcank.curalis.domain.usecase.ResumeMedicationUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
import com.gokcank.curalis.domain.usecase.SearchMedicationsUseCase
import com.gokcank.curalis.domain.usecase.SuspendMedicationUseCase
import com.gokcank.curalis.domain.usecase.UnarchiveMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val searchMedicationsUseCase: SearchMedicationsUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val archiveMedicationUseCase: ArchiveMedicationUseCase,
    private val unarchiveMedicationUseCase: UnarchiveMedicationUseCase,
    private val suspendMedicationUseCase: SuspendMedicationUseCase,
    private val resumeMedicationUseCase: ResumeMedicationUseCase,
    private val generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    // Aktif ve arşivlenmiş ilaçlar aynı akıştan türetilir; Arşiv sekmesi olmadan
    // önce arşivlenen bir ilaç görünmez ve geri alınamazdı — yalnızca kayboluyordu.
    private val _activeMedications = MutableStateFlow<List<Medication>>(emptyList())
    val activeMedications: StateFlow<List<Medication>> = _activeMedications.asStateFlow()

    private val _archivedMedications = MutableStateFlow<List<Medication>>(emptyList())
    val archivedMedications: StateFlow<List<Medication>> = _archivedMedications.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    private var getMedicationsJob: Job? = null

    init {
        getMedications()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            getMedications()
        } else {
            searchMedications(query)
        }
    }

    /** Geçmiş doz kayıtları dahil ilacı tamamen siler. */
    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                cancelAlarmsFor(medication)
                deleteMedicationUseCase(medication)
            } catch (e: Exception) {
                _errorFlow.emit(appContext.getString(R.string.medication_delete_error, e.localizedMessage ?: appContext.getString(R.string.unknown_error)))
            }
        }
    }

    /** İlacı aktif listelerden gizler ama geçmiş doz kayıtlarını korur. */
    fun archiveMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                cancelAlarmsFor(medication)
                archiveMedicationUseCase(medication)
            } catch (e: Exception) {
                _errorFlow.emit(appContext.getString(R.string.medication_delete_error, e.localizedMessage ?: appContext.getString(R.string.unknown_error)))
            }
        }
    }

    /** Arşivdeki bir ilacı Aktif sekmesine geri getirir; hatırlatıcıları ve alarmı yeniden kurar. */
    fun unarchiveMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                unarchiveMedicationUseCase(medication)
                val restored = medication.copy(isArchived = false)
                if (!restored.isSuspended) {
                    generateUpcomingRemindersUseCase(restored)
                    alarmScheduler.scheduleMedicationAlarms(restored)
                }
            } catch (e: Exception) {
                _errorFlow.emit(appContext.getString(R.string.medication_archive_error, e.localizedMessage ?: appContext.getString(R.string.unknown_error)))
            }
        }
    }

    /** İlacı geçici olarak duraklatır — silme/arşivden farklı, listede kalır. */
    fun suspendMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                cancelAlarmsFor(medication)
                suspendMedicationUseCase(medication)
            } catch (e: Exception) {
                _errorFlow.emit(appContext.getString(R.string.medication_suspend_error, e.localizedMessage ?: appContext.getString(R.string.unknown_error)))
            }
        }
    }

    /** Askıya alınmış bir ilacı devam ettirir; hatırlatıcıları ve alarmı yeniden kurar. */
    fun resumeMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                resumeMedicationUseCase(medication)
                val resumed = medication.copy(isSuspended = false)
                generateUpcomingRemindersUseCase(resumed)
                alarmScheduler.scheduleMedicationAlarms(resumed)
            } catch (e: Exception) {
                _errorFlow.emit(appContext.getString(R.string.medication_resume_error, e.localizedMessage ?: appContext.getString(R.string.unknown_error)))
            }
        }
    }

    /** Bir ilaca ait hem gerçek (UUID kimlikli) hem deterministik kimlikli alarmları iptal eder
     *  — silme, arşivleme ve askıya alma akışlarının hepsinde aynı temizlik gerekiyor. */
    private suspend fun cancelAlarmsFor(medication: Medication) {
        getRemindersForMedicationUseCase(medication.id).firstOrNull()?.forEach { reminder ->
            alarmScheduler.cancel(reminder.id)
        }
        alarmScheduler.cancelMedicationAlarms(medication)
    }

    fun takeDose(medicationId: String) {
        viewModelScope.launch {
            try {
                val reminder = Reminder(
                    medicationId = medicationId,
                    timeInMillis = System.currentTimeMillis(),
                    state = ReminderState.TAKEN
                )
                scheduleReminderUseCase(reminder)
                val medicationName = _activeMedications.value.firstOrNull { it.id == medicationId }?.name
                _errorFlow.emit(
                    if (medicationName != null) appContext.getString(R.string.dose_saved_for_medication, medicationName) else appContext.getString(R.string.dose_saved)
                )
            } catch (e: Exception) {
                _errorFlow.emit(appContext.getString(R.string.dose_save_error, e.localizedMessage ?: appContext.getString(R.string.unknown_error)))
            }
        }
    }

    private fun getMedications() {
        observe(getMedicationsUseCase())
    }

    private fun searchMedications(query: String) {
        observe(searchMedicationsUseCase(query))
    }

    /** Aktif/Arşiv akışlarını verilen kaynaktan besler; liste ve arama sorgusu aynı
     *  filtreleme mantığını paylaşır. */
    private fun observe(source: Flow<List<Medication>>) {
        getMedicationsJob?.cancel()
        getMedicationsJob = source.onEach { meds ->
            _activeMedications.value = meds.filter { !it.isArchived }
            _archivedMedications.value = meds.filter { it.isArchived }
        }.launchIn(viewModelScope)
    }
}
