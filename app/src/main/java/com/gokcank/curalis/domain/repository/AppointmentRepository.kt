package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAllAppointments(): Flow<List<Appointment>>
    fun getUpcomingAppointments(currentTime: Long): Flow<List<Appointment>>
    fun getAppointmentById(id: String): Flow<Appointment?>
    suspend fun insertAppointment(appointment: Appointment)
    suspend fun updateAppointment(appointment: Appointment)
    suspend fun deleteAppointment(appointment: Appointment)
}
