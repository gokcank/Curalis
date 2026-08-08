package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.UUID
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
                    
                    val dayCal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                    var shouldTakeToday = false
                    
                    when (medication.frequencyType) {
                        FrequencyType.DAILY -> shouldTakeToday = true
                        FrequencyType.INTERVAL -> {
                            val interval = (medication.intervalDays ?: 2).coerceAtLeast(1)
                            val diffMillis = dayMillis - medStartCal.timeInMillis
                            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                            if (diffDays >= 0 && diffDays % interval == 0) {
                                shouldTakeToday = true
                            }
                        }
                        FrequencyType.SPECIFIC_DAYS -> {
                            fun isoToCalendar(isoDay: Int) = if (isoDay == 7) Calendar.SUNDAY else isoDay + 1
                            val calDayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
                            val specificCalendarDays = medication.specificDays.map { isoToCalendar(it) }
                            if (specificCalendarDays.contains(calDayOfWeek)) {
                                shouldTakeToday = true
                            }
                        }
                        else -> {}
                    }
                    
                    if (shouldTakeToday) {
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
                                            id = UUID.randomUUID().toString(),
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
