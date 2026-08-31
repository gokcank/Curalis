package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetRemindersBetweenDatesUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val medicationRepository: MedicationRepository
) {

    operator fun invoke(start: Long, end: Long): Flow<List<Reminder>> {
        return combine(
            reminderRepository.getRemindersBetweenDates(start, end),
            medicationRepository.getAllMedications()
        ) { dbReminders, medications ->
            
            val virtualReminders = mutableListOf<Reminder>()
            val dbReminderMap = dbReminders.associateBy { "${it.medicationId}_${it.timeInMillis}" }
            // Bir ilacın bir günü için veritabanında zaten en az bir gerçek kayıt varsa,
            // o gün ilacın güncel saatleriyle hayali olarak tekrar doldurulmaz. Aksi halde,
            // örneğin saat değişikliği "yarından itibaren" uygulandığında bugün için
            // bilinçli olarak korunan eski saatteki gerçek kaydın yanına, ilacın yeni
            // saatiyle hayali bir ikinci kayıt daha eklenir (çift doz görünümü).
            val dbReminderDayKeys = dbReminders.mapTo(mutableSetOf()) { reminder ->
                val dayCal = Calendar.getInstance().apply {
                    timeInMillis = reminder.timeInMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                "${reminder.medicationId}_${dayCal.timeInMillis}"
            }

            val startCal = Calendar.getInstance().apply { 
                timeInMillis = start
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = end
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            
            val daysInMillis = mutableListOf<Long>()
            val currentCal = startCal.clone() as Calendar
            while (currentCal.timeInMillis <= endCal.timeInMillis) {
                daysInMillis.add(currentCal.timeInMillis)
                currentCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            medications.forEach { medication ->
                // Arşivlenmiş (silinmiş) ilaçlar için yeni doz üretilmez; zaten kaydedilmiş
                // geçmiş hatırlatıcılar dbReminders üzerinden değişmeden gösterilmeye devam eder.
                if (medication.isArchived) return@forEach
                if (medication.isSuspended) return@forEach
                if (medication.frequencyType == FrequencyType.AS_NEEDED) return@forEach
                if (medication.times.isEmpty()) return@forEach
                
                val medStartCal = Calendar.getInstance().apply {
                    timeInMillis = medication.startDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                daysInMillis.forEach { dayMillis ->
                    if (dayMillis < medStartCal.timeInMillis) return@forEach
                    if ("${medication.id}_$dayMillis" in dbReminderDayKeys) return@forEach

                    if (FrequencyCalculator.shouldTakeOnDay(medication, dayMillis)) {
                        medication.times.forEach { time ->
                            val triggerCal = Calendar.getInstance().apply {
                                timeInMillis = dayMillis
                                set(Calendar.HOUR_OF_DAY, time.hour)
                                set(Calendar.MINUTE, time.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            
                            val timeInMillis = triggerCal.timeInMillis
                            if (timeInMillis in start..end) {
                                val key = "${medication.id}_${timeInMillis}"
                                if (!dbReminderMap.containsKey(key)) {
                                    virtualReminders.add(
                                        Reminder(
                                            id = key,
                                            medicationId = medication.id,
                                            timeInMillis = timeInMillis,
                                            state = ReminderState.SCHEDULED
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            (dbReminders + virtualReminders).sortedBy { it.timeInMillis }
        }
    }
}
