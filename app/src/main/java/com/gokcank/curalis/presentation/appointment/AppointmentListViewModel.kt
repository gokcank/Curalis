package com.gokcank.curalis.presentation.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.usecase.AppointmentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentListViewModel @Inject constructor(
    private val appointmentUseCases: AppointmentUseCases
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

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
