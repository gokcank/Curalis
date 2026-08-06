package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import java.util.Calendar

object FrequencyCalculator {

    fun calculateNextTriggerTime(
        medication: Medication,
        medTime: MedicationTime,
        fromMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (medication.frequencyType == FrequencyType.AS_NEEDED) return null

        val targetCal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            set(Calendar.HOUR_OF_DAY, medTime.hour)
            set(Calendar.MINUTE, medTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (medication.frequencyType) {
            FrequencyType.DAILY -> {
                if (targetCal.timeInMillis <= fromMillis) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                targetCal.timeInMillis
            }

            FrequencyType.INTERVAL -> {
                val interval = (medication.intervalDays ?: 2).coerceAtLeast(1)
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = medication.startDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val diffMillis = targetCal.timeInMillis - startCal.timeInMillis
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                var targetDaysFromStart = if (diffDays <= 0) 0 else diffDays
                val remainder = targetDaysFromStart % interval
                if (remainder != 0) {
                    targetDaysFromStart += (interval - remainder)
                }

                targetCal.timeInMillis = startCal.timeInMillis
                targetCal.add(Calendar.DAY_OF_YEAR, targetDaysFromStart)
                targetCal.set(Calendar.HOUR_OF_DAY, medTime.hour)
                targetCal.set(Calendar.MINUTE, medTime.minute)

                if (targetCal.timeInMillis <= fromMillis) {
                    targetCal.add(Calendar.DAY_OF_YEAR, interval)
                }
                targetCal.timeInMillis
            }

            FrequencyType.SPECIFIC_DAYS -> {
                val specificDays = medication.specificDays
                if (specificDays.isEmpty()) {
                    if (targetCal.timeInMillis <= fromMillis) {
                        targetCal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    return targetCal.timeInMillis
                }

                fun calendarDayToIso(calDay: Int): Int {
                    return if (calDay == Calendar.SUNDAY) 7 else calDay - 1
                }

                var currentIsoDay = calendarDayToIso(targetCal.get(Calendar.DAY_OF_WEEK))

                if (targetCal.timeInMillis <= fromMillis) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                    currentIsoDay = calendarDayToIso(targetCal.get(Calendar.DAY_OF_WEEK))
                }

                var attempts = 0
                while (!specificDays.contains(currentIsoDay) && attempts < 8) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                    currentIsoDay = calendarDayToIso(targetCal.get(Calendar.DAY_OF_WEEK))
                    attempts++
                }
                targetCal.timeInMillis
            }

            FrequencyType.AS_NEEDED -> null
        }
    }
}
