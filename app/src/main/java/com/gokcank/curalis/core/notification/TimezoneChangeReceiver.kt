package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.repository.AppointmentRepository
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Cihazın saat dilimi değiştiğinde (ör. yurt dışı seyahati) tetiklenir. Doz saatleri
 * her zaman cihazın o anki yerel saatine göre hesaplanır (bkz. FrequencyCalculator'ın
 * Calendar.getInstance() kullanımı) — bu yüzden saat dilimi değiştiğinde gelecekteki
 * hatırlatıcıları ve alarmları BootReceiver ile aynı mantıkla yeniden kurmak, "08:00'de
 * al" kuralının eski değil yeni yerel saatte geçerli olmasını sağlar. Randevu ve sabah
 * hatırlatması alarmları da bazı üretici ROM'larının saat dilimi değişiminde alarmları
 * sessizce iptal etmesine karşı önlem olarak yeniden kurulur.
 */
@AndroidEntryPoint
class TimezoneChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var appointmentRepository: AppointmentRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIMEZONE_CHANGED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medications = medicationRepository.getAllMedications().firstOrNull()
                    ?.filter { !it.isArchived && !it.isSuspended } ?: emptyList()
                medications.forEach { medication ->
                    generateUpcomingRemindersUseCase(medication)
                    alarmScheduler.scheduleMedicationAlarms(medication)
                }

                val upcomingAppointments = appointmentRepository
                    .getUpcomingAppointments(System.currentTimeMillis())
                    .firstOrNull() ?: emptyList()
                upcomingAppointments.forEach { appointment ->
                    alarmScheduler.scheduleAppointmentReminder(appointment)
                }

                alarmScheduler.scheduleMorningReminder()

                if (medications.isNotEmpty()) {
                    notificationHelper.showTimezoneChangedNotification()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Saat dilimi değişikliği sonrası hatırlatıcılar yeniden kurulurken hata oluştu", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "TimezoneChangeReceiver"
    }
}
