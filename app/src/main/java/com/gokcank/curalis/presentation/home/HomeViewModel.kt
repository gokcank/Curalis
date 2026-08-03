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
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.repository.VitalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val nextMedication: Pair<Reminder, Medication?>? = null,
    val dailyProgress: Float = 0f,
    val nextAppointment: Appointment? = null,
    val latestVital: Vital? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val appointmentRepository: AppointmentRepository,
    private val vitalRepository: VitalRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
                
            val todayRemindersFlow = reminderRepository.getRemindersBetweenDates(startOfDay, endOfDay)
            
            combine(appointmentFlow, vitalFlow, todayRemindersFlow) { nextAppt, latestVital, todayReminders ->
                val total = todayReminders.size
                val taken = todayReminders.count { it.state == ReminderState.TAKEN }
                val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f
                
                val nextReminder = todayReminders
                    .filter { it.timeInMillis >= now && it.state != ReminderState.TAKEN }
                    .minByOrNull { it.timeInMillis }
                    
                var nextMedInfo: Pair<Reminder, Medication?>? = null
                if (nextReminder != null) {
                    val med = medicationRepository.getMedicationById(nextReminder.medicationId).firstOrNull()
                    nextMedInfo = nextReminder to med
                }
                
                HomeUiState(
                    nextMedication = nextMedInfo,
                    dailyProgress = progress,
                    nextAppointment = nextAppt,
                    latestVital = latestVital,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
