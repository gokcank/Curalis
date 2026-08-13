package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.AppointmentRepository
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersForMedicationUseCase
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

    @Inject
    lateinit var getRemindersForMedicationUseCase: GetRemindersForMedicationUseCase

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val medications = medicationRepository.getAllMedications().firstOrNull()
                        ?.filter { !it.isArchived } ?: emptyList()
                    val now = System.currentTimeMillis()
                    medications.forEach { medication ->
                        // Reboot, kurulu tüm AlarmManager alarmlarını temizler; bu yüzden
                        // zamanı geçmiş ama hâlâ SCHEDULED durumunda kalan bir hatırlatıcının
                        // alarmı artık kesin olarak kaybolmuştur — tetiklenmeyi bekleyen bir
                        // "hayalet" kayıt olarak kalmasın diye MISSED olarak işaretlenir.
                        getRemindersForMedicationUseCase(medication.id).firstOrNull()
                            ?.filter { it.state == ReminderState.SCHEDULED && it.timeInMillis < now }
                            ?.forEach { staleReminder ->
                                reminderRepository.updateReminder(staleReminder.copy(state = ReminderState.MISSED))
                            }

                        // Cihaz uzun süre kapalıysa 30 günlük pencerenin bir kısmı geride
                        // kalmış olabilir; burada da çalıştırmak pencereyi ileriye taşır.
                        generateUpcomingRemindersUseCase(medication)
                        alarmScheduler.scheduleMedicationAlarms(medication)
                    }

                    val upcomingAppointments = appointmentRepository
                        .getUpcomingAppointments(System.currentTimeMillis())
                        .firstOrNull() ?: emptyList()
                    upcomingAppointments.forEach { appointment ->
                        alarmScheduler.scheduleAppointmentReminder(appointment)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Reboot sonrası hatırlatıcılar yeniden kurulurken hata oluştu", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
