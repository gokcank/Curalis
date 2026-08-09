package com.gokcank.curalis.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.usecase.FrequencyCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Android 12+ (API 31) kullanıcının rızasını geri alabildiği için, her planlamadan
     * önce kontrol edilmeli; izin yoksa alarm sessizce atlanır.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun setExactAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent): Boolean {
        if (!canScheduleExactAlarms()) {
            Log.w(TAG, "Tam zamanlı alarm izni yok, alarm kurulamadı.")
            return false
        }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Alarm kurulurken izin hatası", e)
            false
        }
    }

    fun schedule(reminder: Reminder, medicationName: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MEDICATION_ID, reminder.medicationId)
            putExtra(EXTRA_TIME_MILLIS, reminder.timeInMillis)
            putExtra(EXTRA_REMINDER_KIND, KIND_MEDICATION)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (reminder.timeInMillis > System.currentTimeMillis()) {
            setExactAlarm(reminder.timeInMillis, pendingIntent)
        }
    }

    fun scheduleMedicationAlarms(medication: Medication) {
        if (medication.frequencyType == FrequencyType.AS_NEEDED) return

        medication.times.forEach { medTime ->
            val nextTriggerMillis = FrequencyCalculator.calculateNextTriggerTime(medication, medTime) ?: return@forEach
            val reminderId = "${medication.id}_${medTime.id}"

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_MEDICATION_NAME, medication.name)
                putExtra(EXTRA_MEDICATION_ID, medication.id)
                putExtra(EXTRA_DOSE, medTime.dose)
                putExtra(EXTRA_TIME_MILLIS, nextTriggerMillis)
                putExtra(EXTRA_REMINDER_KIND, KIND_MEDICATION)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setExactAlarm(nextTriggerMillis, pendingIntent)
        }
    }

    /**
     * Bir ilaca ait tüm saatler için kurulmuş alarmları iptal eder.
     * İlaç silindiğinde veya saatleri güncellenmeden önce çağrılmalıdır.
     */
    fun cancelMedicationAlarms(medication: Medication) {
        medication.times.forEach { medTime ->
            cancel("${medication.id}_${medTime.id}")
        }
    }

    fun scheduleAppointmentReminder(appointment: Appointment) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, appointment.id)
            putExtra(EXTRA_MEDICATION_NAME, "Randevu: ${appointment.title}")
            putExtra(EXTRA_MEDICATION_ID, appointment.doctorId ?: "")
            putExtra(EXTRA_REMINDER_KIND, KIND_APPOINTMENT)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointment.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reminderTime = if (appointment.timeInMillis - (60 * 60 * 1000) > System.currentTimeMillis()) {
            appointment.timeInMillis - (60 * 60 * 1000)
        } else {
            appointment.timeInMillis
        }

        if (reminderTime > System.currentTimeMillis()) {
            setExactAlarm(reminderTime, pendingIntent)
        }
    }

    fun scheduleMissedDoseCheck(reminderId: String, medicationId: String, delayMinutes: Int = 30) {
        val intent = Intent(context, MissedDoseCheckReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
        }

        val requestCode = (reminderId + "_missed").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        setExactAlarm(triggerAtMillis, pendingIntent)
    }

    fun cancel(reminderId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_DOSE = "extra_dose"
        const val EXTRA_TIME_MILLIS = "extra_time_millis"
        /** İlaç hatırlatıcısı ile randevu hatırlatıcısını ayırt eder; ikisi de aynı
         *  ReminderReceiver'ı kullanır ama yalnızca ilaç hatırlatıcıları Reminder
         *  tablosunda durum takibi (DELIVERED/MISSED) alır. */
        const val EXTRA_REMINDER_KIND = "extra_reminder_kind"
        const val KIND_MEDICATION = "medication"
        const val KIND_APPOINTMENT = "appointment"
    }
}
