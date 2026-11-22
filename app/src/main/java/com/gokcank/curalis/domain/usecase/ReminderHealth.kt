package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.repository.ReminderSystemStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Ana ekrandaki uyarı şeridinde gösterilen, hatırlatmaların güvenilirliğini bozan durumlar. */
data class ReminderHealthReport(
    /** Saati geçtiği halde alarmı hiç çalmamış doz sayısı. */
    val undeliveredDoseCount: Int = 0,
    val notificationsBlocked: Boolean = false,
    val exactAlarmsDenied: Boolean = false,
    val batteryOptimized: Boolean = false
) {
    val hasIssues: Boolean
        get() = undeliveredDoseCount > 0 || notificationsBlocked || exactAlarmsDenied || batteryOptimized
}

/**
 * Hatırlatmaların gerçekten gelip gelmediğini ve gelmesi için gereken izinlerin açık olup
 * olmadığını değerlendirir. Hiçbir doz ya da ayar değiştirilmez; yalnızca durum raporlanır.
 */
object ReminderHealth {

    /** Alarmın çalıp dozu "iletildi" yapması için tanınan pay. */
    const val GRACE_MILLIS = 15 * 60 * 1000L

    /** Geriye doğru bakılan süre; daha eski dozlar uyarıya girmez. */
    const val LOOKBACK_MILLIS = 24 * 60 * 60 * 1000L

    /**
     * Saati (yeniden planlandıysa yeni saati) [GRACE_MILLIS]'ten daha önce geçmiş, son
     * [LOOKBACK_MILLIS] içinde kalan ve hâlâ SCHEDULED olan dozlar: alarm çalsaydı doz en az
     * DELIVERED olurdu (bkz. ReminderState). Alarm kurulmayan dozlar sayılmaz: plasebo günleri,
     * ihtiyaç halinde alınan ilaçlar, askıdaki / arşivlenmiş / silinmiş ilaçlar ve tedavi
     * bitişinden sonraki dozlar.
     */
    fun undeliveredDoses(
        reminders: List<Reminder>,
        medications: List<Medication>,
        now: Long
    ): List<Reminder> {
        val remindable = medications
            .filter { hasReminderAlarms(it) }
            .associateBy { it.id }
        return reminders.filter { reminder ->
            val medication = remindable[reminder.medicationId] ?: return@filter false
            if (reminder.state != ReminderState.SCHEDULED || reminder.isPlacebo) return@filter false
            val treatmentEnd = FrequencyCalculator.treatmentEndMillis(medication)
            if (treatmentEnd != null && reminder.timeInMillis >= treatmentEnd) return@filter false
            val effectiveTime = reminder.rescheduledToMillis ?: reminder.timeInMillis
            effectiveTime < now - GRACE_MILLIS && effectiveTime >= now - LOOKBACK_MILLIS
        }
    }

    /**
     * İzin sorunları yalnızca alarm kuran en az bir ilaç varken raporlanır; hatırlatması
     * olmayan kullanıcıya izin uyarısı gösterilmez.
     */
    fun report(
        reminders: List<Reminder>,
        medications: List<Medication>,
        now: Long,
        notificationsEnabled: Boolean,
        exactAlarmsAllowed: Boolean,
        batteryOptimizationIgnored: Boolean
    ): ReminderHealthReport {
        val hasRemindableMedication = medications.any { hasReminderAlarms(it) }
        return ReminderHealthReport(
            undeliveredDoseCount = undeliveredDoses(reminders, medications, now).size,
            notificationsBlocked = hasRemindableMedication && !notificationsEnabled,
            exactAlarmsDenied = hasRemindableMedication && !exactAlarmsAllowed,
            batteryOptimized = hasRemindableMedication && !batteryOptimizationIgnored
        )
    }

    private fun hasReminderAlarms(medication: Medication): Boolean =
        !medication.isArchived && !medication.isSuspended &&
            medication.frequencyType != FrequencyType.AS_NEEDED
}

/**
 * [now] anına göre hatırlatma sağlığını izler; dozlar yanıtlandıkça rapor kendiliğinden
 * güncellenir. İzinler her yeni değerde yeniden okunur. Yalnızca veritabanındaki gerçek doz
 * kayıtlarına bakılır: GetRemindersBetweenDatesUseCase'in ürettiği hayali dozların hiç alarmı
 * olmadığı için burada kullanılmaz.
 */
class ObserveReminderHealthUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val medicationRepository: MedicationRepository,
    private val reminderSystemStatus: ReminderSystemStatus
) {
    operator fun invoke(now: Long): Flow<ReminderHealthReport> =
        combine(
            // Yeniden planlanmış bir doz planlanan saatinden sonra pencereye girebilir; bu
            // yüzden sorgu penceresi bakılan sürenin iki katı tutulur, süzme yeni saate göre yapılır.
            reminderRepository.getRemindersBetweenDates(now - 2 * ReminderHealth.LOOKBACK_MILLIS, now),
            medicationRepository.getAllMedications()
        ) { reminders, medications ->
            ReminderHealth.report(
                reminders = reminders,
                medications = medications,
                now = now,
                notificationsEnabled = reminderSystemStatus.areReminderNotificationsEnabled(),
                exactAlarmsAllowed = reminderSystemStatus.canScheduleExactAlarms(),
                batteryOptimizationIgnored = reminderSystemStatus.isIgnoringBatteryOptimizations()
            )
        }
}
