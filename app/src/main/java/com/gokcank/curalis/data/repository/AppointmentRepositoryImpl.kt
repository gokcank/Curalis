package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.AppointmentDao
import com.gokcank.curalis.data.local.entity.AppointmentEntity
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao
) : AppointmentRepository {

    override fun getAllAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getAllAppointments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUpcomingAppointments(currentTime: Long): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(currentTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAppointmentById(id: String): Flow<Appointment?> {
        return appointmentDao.getAppointmentById(id).map { it?.toDomain() }
    }

    override suspend fun insertAppointment(appointment: Appointment) {
        appointmentDao.insertAppointment(appointment.toEntity())
    }

    override suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment.toEntity())
    }

    override suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.deleteAppointment(appointment.toEntity())
    }
}

fun AppointmentEntity.toDomain(): Appointment {
    return Appointment(
        id = id,
        doctorId = doctorId,
        title = title,
        timeInMillis = timeInMillis,
        location = location,
        notes = notes
    )
}

fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        id = id,
        doctorId = doctorId,
        title = title,
        timeInMillis = timeInMillis,
        location = location,
        notes = notes
    )
}
