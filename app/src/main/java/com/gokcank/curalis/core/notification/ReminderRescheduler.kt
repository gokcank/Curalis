package com.gokcank.curalis.core.notification

import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.repository.AppointmentRepository
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.VitalReminderRepository
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Replaces every reminder alarm after the database contents are swapped wholesale (a backup
 * restore). AlarmManager alarms live outside the database: without this, alarms for the
 * replaced data keep firing and the restored data gets no alarms until the daily
 * ReminderWindowWorker or a reboot happens to run.
 */
class ReminderRescheduler @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val appointmentRepository: AppointmentRepository,
    private val vitalReminderRepository: VitalReminderRepository,
    private val generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase,
    private val alarmScheduler: AlarmScheduler
) {

    /** The alarms that exist for the current data, captured before that data is replaced. */
    class Snapshot internal constructor(
        internal val medications: List<Medication>,
        internal val appointments: List<Appointment>
    )

    suspend fun snapshot(): Snapshot = Snapshot(
        medications = medicationRepository.getAllMedications().firstOrNull().orEmpty(),
        appointments = appointmentRepository
            .getUpcomingAppointments(System.currentTimeMillis())
            .firstOrNull().orEmpty()
    )

    fun cancel(snapshot: Snapshot) {
        snapshot.medications.forEach { alarmScheduler.cancelMedicationAlarms(it) }
        snapshot.appointments.forEach { alarmScheduler.cancel(it.id) }
        VitalType.entries.forEach { alarmScheduler.cancelVitalReminder(it) }
    }

    suspend fun scheduleAll() {
        medicationRepository.getAllMedications().firstOrNull().orEmpty()
            .filter { !it.isArchived && !it.isSuspended }
            .forEach { medication ->
                generateUpcomingRemindersUseCase(medication)
                alarmScheduler.scheduleMedicationAlarms(medication)
            }

        appointmentRepository.getUpcomingAppointments(System.currentTimeMillis())
            .firstOrNull().orEmpty()
            .forEach { alarmScheduler.scheduleAppointmentReminder(it) }

        vitalReminderRepository.getAllSettings().firstOrNull().orEmpty()
            .filter { it.enabled }
            .forEach { alarmScheduler.scheduleVitalReminder(it.type, it.hour, it.minute, it.daysOfWeek) }

        alarmScheduler.scheduleMorningReminder()
    }
}
