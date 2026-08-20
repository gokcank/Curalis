package com.gokcank.curalis.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.repository.AppointmentRepository
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.VitalRepository
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** Ana ekrandaki "Bugünün Kutusu" pillbox şeridindeki tek bir doz. */
data class HomePillboxItem(
    val reminder: Reminder,
    val medication: Medication?
)

data class HomeUiState(
    val nextMedication: Pair<Reminder, Medication?>? = null,
    val dailyProgress: Float = 0f,
    val nextAppointment: Appointment? = null,
    val latestVital: Vital? = null,
    /** Bugün için hiç doz planlanmışsa true — günün son dozu geçmiş olsa bile "Bugünkü
     *  Program" kartının kaybolmaması, kullanıcının geçmiş bir dozu düzeltebilmek için
     *  o ekrana ulaşabilmesi gerekiyor. */
    val hasTodayReminders: Boolean = false,
    val pillboxItems: List<HomePillboxItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRemindersBetweenDatesUseCase: GetRemindersBetweenDatesUseCase,
    private val appointmentRepository: AppointmentRepository,
    private val vitalRepository: VitalRepository,
    private val medicationRepository: MedicationRepository,
    private val acknowledgeReminderUseCase: AcknowledgeReminderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Rapor içeriği diyaloğundaki ilaç filtresi dropdown'unu doldurmak için. */
    val medications: StateFlow<List<Medication>> = medicationRepository.getAllMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis

            val appointmentFlow = appointmentRepository.getUpcomingAppointments(now)
                .map { appointments -> appointments.firstOrNull() }
                
            val vitalFlow = vitalRepository.getAllVitals()
                .map { vitals -> vitals.firstOrNull() }
                
            val todayRemindersFlow = getRemindersBetweenDatesUseCase(startOfDay, endOfDay)

            combine(appointmentFlow, vitalFlow, todayRemindersFlow, medications) { nextAppt, latestVital, todayReminders, meds ->
                // Plasebo dozları (CYCLIC dinlenme günleri) uyum ilerlemesine dahil edilmez —
                // atlanmalarının klinik bir karşılığı yoktur, bkz. Reminder.isPlacebo.
                val adherenceReminders = todayReminders.filterNot { it.isPlacebo }
                val total = adherenceReminders.size
                val taken = adherenceReminders.count { it.state == ReminderState.TAKEN }
                val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f

                val nextReminder = todayReminders
                    .filter { it.timeInMillis >= now && it.state != ReminderState.TAKEN }
                    .minByOrNull { it.timeInMillis }

                var nextMedInfo: Pair<Reminder, Medication?>? = null
                if (nextReminder != null) {
                    val med = medicationRepository.getMedicationById(nextReminder.medicationId).firstOrNull()
                    nextMedInfo = nextReminder to med
                }

                val medsMap = meds.associateBy { it.id }
                val pillboxItems = todayReminders
                    .sortedBy { it.timeInMillis }
                    .map { HomePillboxItem(reminder = it, medication = medsMap[it.medicationId]) }

                HomeUiState(
                    nextMedication = nextMedInfo,
                    dailyProgress = progress,
                    nextAppointment = nextAppt,
                    latestVital = latestVital,
                    hasTodayReminders = total > 0,
                    pillboxItems = pillboxItems,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun acknowledgeDose(reminder: Reminder, newState: ReminderState, takenAtMillis: Long? = null) {
        viewModelScope.launch {
            acknowledgeReminderUseCase(reminder, newState, takenAtMillis = takenAtMillis)
        }
    }
}
