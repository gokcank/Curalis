package com.gokcank.curalis.presentation.medication.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.usecase.ArchiveMedicationUseCase
import com.gokcank.curalis.domain.usecase.DeleteMedicationUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersForMedicationUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
import com.gokcank.curalis.domain.usecase.SearchMedicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val searchMedicationsUseCase: SearchMedicationsUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val archiveMedicationUseCase: ArchiveMedicationUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

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
                _errorFlow.emit("İlaç silinirken bir hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}")
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
                _errorFlow.emit("İlaç silinirken bir hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}")
            }
        }
    }

    /** Bir ilaca ait hem gerçek (UUID kimlikli) hem deterministik kimlikli alarmları iptal eder
     *  — silme ve arşivleme akışlarının ikisinde de aynı temizlik gerekiyor. */
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
            } catch (e: Exception) {
                _errorFlow.emit("Doz kaydedilirken bir hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}")
            }
        }
    }

    private fun getMedications() {
        observe(getMedicationsUseCase())
    }

    private fun searchMedications(query: String) {
        observe(searchMedicationsUseCase(query))
    }

    /** `medications` akışını verilen kaynaktan besler; liste ve arama sorgusu aynı
     *  filtreleme/iptal mantığını paylaşır. */
    private fun observe(source: Flow<List<Medication>>) {
        getMedicationsJob?.cancel()
        getMedicationsJob = source.onEach { meds ->
            _medications.value = meds.filter { !it.isArchived }
        }.launchIn(viewModelScope)
    }
}
