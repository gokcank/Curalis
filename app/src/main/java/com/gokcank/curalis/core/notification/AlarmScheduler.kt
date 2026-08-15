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
import java.util.Calendar
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences
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
            // Tam zamanlı alarm izni yoksa hatırlatmayı tamamen atlamak yerine, birkaç
            // dakikalık sapma göze alınarak Android'in yaklaşık zamanlı alarmına düşülür —
            // kullanıcı hiç hatırlatma almamaktan iyi.
            Log.w(TAG, "Tam zamanlı alarm izni yok, yaklaşık zamanlı alarma düşülüyor.")
            return setInexactAlarm(triggerAtMillis, pendingIntent)
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
            Log.e(TAG, "Alarm kurulurken izin hatası, yaklaşık zamanlı alarma düşülüyor.", e)
            setInexactAlarm(triggerAtMillis, pendingIntent)
        }
    }

    private fun setInexactAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Yaklaşık zamanlı alarm da kurulamadı.", e)
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
            // Bu ilaç+saat dilimi için AlarmManager'daki alarmın kimliği (iptal ederken de
            // kullanılır, bkz. cancelMedicationAlarms) — gün değişse de sabit kalır.
            val pendingIntentKey = "${medication.id}_${medTime.id}"
            // Veritabanındaki Reminder satırının kimliği — GenerateUpcomingRemindersUseCase'in
            // önceden yazdığı satırla aynı şema (medicationId_timeInMillis) kullanılır ki alarm
            // çaldığında ReminderReceiver ayrı bir kayıt oluşturmak yerine o satırı güncellesin.
            val dbReminderId = "${medication.id}_$nextTriggerMillis"

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, dbReminderId)
                putExtra(EXTRA_MEDICATION_NAME, medication.name)
                putExtra(EXTRA_MEDICATION_ID, medication.id)
                putExtra(EXTRA_DOSE, medTime.dose)
                putExtra(EXTRA_TIME_MILLIS, nextTriggerMillis)
                putExtra(EXTRA_REMINDER_KIND, KIND_MEDICATION)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pendingIntentKey.hashCode(),
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

        val leadMillis = notificationPreferences.appointmentReminderMinutesBefore * 60 * 1000L
        val reminderTime = if (appointment.timeInMillis - leadMillis > System.currentTimeMillis()) {
            appointment.timeInMillis - leadMillis
        } else {
            appointment.timeInMillis
        }

        if (reminderTime > System.currentTimeMillis()) {
            setExactAlarm(reminderTime, pendingIntent)
        }
    }

    fun scheduleMissedDoseCheck(
        reminderId: String,
        medicationId: String,
        medicationName: String,
        delayMinutes: Int,
        attempt: Int
    ) {
        val intent = Intent(context, MissedDoseCheckReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MISSED_CHECK_ATTEMPT, attempt)
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

    private fun morningReminderPendingIntent(): PendingIntent {
        val intent = Intent(context, MorningReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            MORNING_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Bir sonraki sabah hatırlatmasının tetikleneceği zamanı, hafta içi/hafta sonu ayrımını
     * gözeterek hesaplar. Tek seferlik alarm olarak kurulur; [MorningReminderReceiver] her
     * tetiklendiğinde bir sonraki günü yeniden kurar — AlarmManager'ın günlük tekrar eden
     * alarmları hafta içi/sonu ayrımı yapamadığı için bu yaklaşım tercih edildi.
     */
    private fun nextMorningReminderTrigger(): Long {
        fun targetMinutesFor(cal: Calendar): Int {
            val isWeekend = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            return if (isWeekend && notificationPreferences.weekendModeEnabled) {
                notificationPreferences.weekendMorningReminderMinutes
            } else {
                notificationPreferences.morningReminderMinutes
            }
        }

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        var targetMinutes = targetMinutesFor(cal)
        cal.set(Calendar.HOUR_OF_DAY, targetMinutes / 60)
        cal.set(Calendar.MINUTE, targetMinutes % 60)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            targetMinutes = targetMinutesFor(cal)
            cal.set(Calendar.HOUR_OF_DAY, targetMinutes / 60)
            cal.set(Calendar.MINUTE, targetMinutes % 60)
        }

        return cal.timeInMillis
    }

    /** Sabah hatırlatması kapalıysa kurulu alarmı iptal eder; açıksa bir sonraki tetiklenmeyi kurar. */
    fun scheduleMorningReminder() {
        if (!notificationPreferences.morningReminderEnabled) {
            cancelMorningReminder()
            return
        }
        setExactAlarm(nextMorningReminderTrigger(), morningReminderPendingIntent())
    }

    fun cancelMorningReminder() {
        alarmManager.cancel(morningReminderPendingIntent())
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
        const val EXTRA_MISSED_CHECK_ATTEMPT = "extra_missed_check_attempt"
        /** İlaç hatırlatıcısı ile randevu hatırlatıcısını ayırt eder; ikisi de aynı
         *  ReminderReceiver'ı kullanır ama yalnızca ilaç hatırlatıcıları Reminder
         *  tablosunda durum takibi (DELIVERED/MISSED) alır. */
        const val EXTRA_REMINDER_KIND = "extra_reminder_kind"
        const val KIND_MEDICATION = "medication"
        const val KIND_APPOINTMENT = "appointment"
        private const val MORNING_REMINDER_REQUEST_CODE = -2001
    }
}
