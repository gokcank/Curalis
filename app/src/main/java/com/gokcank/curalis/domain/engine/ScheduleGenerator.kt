package com.gokcank.curalis.domain.engine

import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.ReminderState
import java.util.Calendar
import java.util.UUID

object ScheduleGenerator {

    /**
     * Generates ReminderEvent entities for the next [daysAhead] days based on the medication rules.
     */
    fun generateReminders(
        medication: MedicationEntity,
        days: List<MedicationDaysEntity>,
        times: List<MedicationTimeEntity>,
        daysAhead: Int = 14
    ): List<ReminderEntity> {
        val reminders = mutableListOf<ReminderEntity>()
        
        if (medication.frequencyType == FrequencyType.AS_NEEDED.name) {
            return emptyList() // No scheduled reminders for PRN
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = medication.startDate
        
        // Reset to midnight for clean day calculations
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startDateMidnight = calendar.timeInMillis
        val currentCalendar = Calendar.getInstance()
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
        currentCalendar.set(Calendar.MINUTE, 0)
        currentCalendar.set(Calendar.SECOND, 0)
        currentCalendar.set(Calendar.MILLISECOND, 0)
        
        // Ensure we don't generate deeply in the past. Start from today or startDate, whichever is later.
        if (startDateMidnight > currentCalendar.timeInMillis) {
            currentCalendar.timeInMillis = startDateMidnight
        }

        for (i in 0 until daysAhead) {
            val dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)
            val isoDayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

            val shouldGenerateToday = when (medication.frequencyType) {
                FrequencyType.DAILY.name -> true
                FrequencyType.SPECIFIC_DAYS.name -> days.any { it.dayOfWeek == isoDayOfWeek }
                FrequencyType.INTERVAL.name -> {
                    val diffInMillis = currentCalendar.timeInMillis - startDateMidnight
                    val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                    val interval = medication.intervalDays ?: 1
                    diffInDays % interval == 0
                }
                else -> false
            }

            if (shouldGenerateToday) {
                times.forEach { time ->
                    val eventCalendar = Calendar.getInstance()
                    eventCalendar.timeInMillis = currentCalendar.timeInMillis
                    eventCalendar.set(Calendar.HOUR_OF_DAY, time.hour)
                    eventCalendar.set(Calendar.MINUTE, time.minute)
                    eventCalendar.set(Calendar.SECOND, 0)
                    
                    reminders.add(
                        ReminderEntity(
                            id = UUID.randomUUID().toString(),
                            medicationId = medication.id,
                            timeInMillis = eventCalendar.timeInMillis,
                            state = ReminderState.SCHEDULED.name
                        )
                    )
                }
            }
            
            currentCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return reminders
    }
}
