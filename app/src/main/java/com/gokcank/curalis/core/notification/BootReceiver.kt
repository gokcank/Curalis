package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gokcank.curalis.domain.repository.AppointmentRepository
import com.gokcank.curalis.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var appointmentRepository: AppointmentRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val medications = medicationRepository.getAllMedications().firstOrNull()
                        ?.filter { !it.isArchived } ?: emptyList()
                    medications.forEach { medication ->
                        alarmScheduler.scheduleMedicationAlarms(medication)
                    }

                    val upcomingAppointments = appointmentRepository
                        .getUpcomingAppointments(System.currentTimeMillis())
                        .firstOrNull() ?: emptyList()
                    upcomingAppointments.forEach { appointment ->
                        alarmScheduler.scheduleAppointmentReminder(appointment)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
