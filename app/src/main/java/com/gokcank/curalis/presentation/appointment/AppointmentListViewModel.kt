package com.gokcank.curalis.presentation.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.usecase.AppointmentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentListViewModel @Inject constructor(
    private val appointmentUseCases: AppointmentUseCases
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

    // "Yaklaşan" (henüz geçmemiş) / "Tamamlandı" (zamanı geçmiş) sekmeleri, isVisited
    // notundan bağımsız olarak yalnızca randevu saatine göre ayrılır.
    val upcomingAppointments: StateFlow<List<Appointment>> = _appointments
        .map { list -> list.filter { it.timeInMillis >= System.currentTimeMillis() }.sortedBy { it.timeInMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedAppointments: StateFlow<List<Appointment>> = _appointments
        .map { list -> list.filter { it.timeInMillis < System.currentTimeMillis() }.sortedByDescending { it.timeInMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            appointmentUseCases.getAppointments().collect { appointmentList ->
                _appointments.value = appointmentList
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentUseCases.deleteAppointment(appointment)
        }
    }
}
