package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.Reminder
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

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var vitalReminderRepository: com.gokcank.curalis.domain.repository.VitalReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val medications = medicationRepository.getAllMedications().firstOrNull()
                        ?.filter { !it.isArchived && !it.isSuspended } ?: emptyList()
                    val now = System.currentTimeMillis()
                    var missedWhileOff = 0
                    medications.forEach { medication ->
                        // Reboot, kurulu tüm AlarmManager alarmlarını temizler; bu yüzden
                        // zamanı geçmiş ama hâlâ SCHEDULED durumunda kalan bir hatırlatıcının
                        // alarmı artık kesin olarak kaybolmuştur — tetiklenmeyi bekleyen bir
                        // "hayalet" kayıt olarak kalmasın diye MISSED olarak işaretlenir.
                        val staleReminders = staleScheduledDoses(
                            getRemindersForMedicationUseCase(medication.id).firstOrNull().orEmpty(),
                            now
                        )
                        staleReminders.forEach { staleReminder ->
                            reminderRepository.updateReminder(staleReminder.copy(state = ReminderState.MISSED))
                        }
                        missedWhileOff += countMissedWhileOff(staleReminders)

                        // Cihaz uzun süre kapalıysa 30 günlük pencerenin bir kısmı geride
                        // kalmış olabilir; burada da çalıştırmak pencereyi ileriye taşır.
                        generateUpcomingRemindersUseCase(medication)
                        alarmScheduler.scheduleMedicationAlarms(medication)
                    }

                    // Yalnızca telefon gerçekten yeniden açıldığında: uygulama güncellemesinde
                    // (MY_PACKAGE_REPLACED) telefon kapanmamıştır.
                    if (intent.action == Intent.ACTION_BOOT_COMPLETED && missedWhileOff > 0) {
                        notificationHelper.showMissedWhileOffNotification(missedWhileOff)
                    }

                    val upcomingAppointments = appointmentRepository
                        .getUpcomingAppointments(System.currentTimeMillis())
                        .firstOrNull() ?: emptyList()
                    upcomingAppointments.forEach { appointment ->
                        alarmScheduler.scheduleAppointmentReminder(appointment)
                    }

                    // Reboot, sabah hatırlatması alarmını da siler.
                    alarmScheduler.scheduleMorningReminder()
                    alarmScheduler.scheduleEveningSummary()

                    // Reboot, ölçüm hatırlatıcı alarmlarını da siler.
                    vitalReminderRepository.getAllSettings().firstOrNull()
                        ?.filter { it.enabled }
                        ?.forEach { setting ->
                            alarmScheduler.scheduleVitalReminder(setting.type, setting.hour, setting.minute, setting.daysOfWeek)
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

/**
 * Yeniden başlatmada alarmı kesin olarak kaybolmuş dozlar: zamanı (yeniden planlandıysa yeni
 * zamanı) geçmiş ama hâlâ SCHEDULED olanlar. Yeni zamanı henüz gelmemiş yeniden planlanmış doz
 * kaçırılmış sayılmaz; alarmı yeniden kurulur.
 */
internal fun staleScheduledDoses(reminders: List<Reminder>, now: Long): List<Reminder> =
    reminders.filter { it.state == ReminderState.SCHEDULED && (it.rescheduledToMillis ?: it.timeInMillis) < now }

/** "Telefon kapalıyken kaçırıldı" bildirimine sayılan dozlar; plasebo dozları uyuma girmediği için sayılmaz. */
internal fun countMissedWhileOff(staleReminders: List<Reminder>): Int =
    staleReminders.count { !it.isPlacebo }
