package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * Akşam özetinde sayılan dozlar: bugün saati (yeniden planlandıysa yeni saati) özet anına
 * kadar gelmiş ve hâlâ işaretlenmemiş dozlar. Alarmı hiç çalmamış, çalıp yanıtlanmamış ve
 * kaçırıldı sayılmış dozlar girer; alınan, atlanan ve ertelenen (yerine yeni kayıt açılan)
 * dozlar girmez. Özet saatinden sonraya planlanmış dozlar henüz gelmediği için sayılmaz.
 * Plasebo günleri, ihtiyaç halinde alınan ilaçlar ve askıdaki / arşivlenmiş ilaçlar dışarıda kalır.
 */
object EveningSummary {

    private val UNMARKED_STATES = setOf(ReminderState.SCHEDULED, ReminderState.DELIVERED, ReminderState.MISSED)

    fun unmarkedDoses(
        reminders: List<Reminder>,
        medications: List<Medication>,
        now: Long,
        startOfToday: Long
    ): List<Reminder> {
        val activeIds = medications
            .filter { !it.isArchived && !it.isSuspended && it.frequencyType != FrequencyType.AS_NEEDED }
            .mapTo(mutableSetOf()) { it.id }
        return reminders.filter { reminder ->
            val effectiveTime = reminder.rescheduledToMillis ?: reminder.timeInMillis
            reminder.medicationId in activeIds &&
                reminder.state in UNMARKED_STATES &&
                !reminder.isPlacebo &&
                effectiveTime in startOfToday..now
        }
    }

    fun startOfDay(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Bugün [now] anına kadar işaretlenmemiş doz sayısı. Zaman çizelgesiyle aynı doz listesine
 * (veritabanındaki kayıtlar ve henüz yazılmamış dozlar) bakar ki özet, kullanıcının çizelgede
 * gördüğüyle tutarlı olsun.
 */
class CountUnmarkedDosesTodayUseCase @Inject constructor(
    private val getRemindersBetweenDates: GetRemindersBetweenDatesUseCase,
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(now: Long): Int {
        val startOfToday = EveningSummary.startOfDay(now)
        val reminders = getRemindersBetweenDates(startOfToday, now).first()
        val medications = medicationRepository.getAllMedications().first()
        return EveningSummary.unmarkedDoses(reminders, medications, now, startOfToday).size
    }
}
