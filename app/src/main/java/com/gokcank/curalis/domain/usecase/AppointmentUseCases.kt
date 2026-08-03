package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppointmentUseCases @Inject constructor(
    val getAppointments: GetAppointmentsUseCase,
    val getAppointmentById: GetAppointmentByIdUseCase,
    val getUpcomingAppointments: GetUpcomingAppointmentsUseCase,
    val addAppointment: AddAppointmentUseCase,
    val updateAppointment: UpdateAppointmentUseCase,
    val deleteAppointment: DeleteAppointmentUseCase
)

class GetAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(): Flow<List<Appointment>> {
        return repository.getAllAppointments()
    }
}

class GetUpcomingAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(currentTime: Long): Flow<List<Appointment>> {
        return repository.getUpcomingAppointments(currentTime)
    }
}

class GetAppointmentByIdUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    operator fun invoke(id: String): Flow<Appointment?> {
        return repository.getAppointmentById(id)
    }
}

class AddAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(appointment: Appointment) {
        repository.insertAppointment(appointment)
    }
}

class DeleteAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(appointment: Appointment) {
        repository.deleteAppointment(appointment)
    }
}

class UpdateAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(appointment: Appointment) {
        repository.updateAppointment(appointment)
    }
}
