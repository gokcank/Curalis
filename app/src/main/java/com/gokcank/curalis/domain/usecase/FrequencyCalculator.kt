package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar

object FrequencyCalculator {

    /**
     * İki zaman damgası arasındaki takvim gün farkını, yaz saati geçişlerinde bazı günlerin
     * 23 veya 25 saat sürebildiğini hesaba katarak hesaplar. Sabit "milisaniye / (24*60*60*1000)"
     * bölmesi DST geçişlerinde günü bir kaydırabilir.
     */
    private fun calendarDaysBetween(startMillis: Long, endMillis: Long): Int {
        val zone = ZoneId.systemDefault()
        val startDate = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate()
        val endDate = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate()
        return ChronoUnit.DAYS.between(startDate, endDate).toInt()
    }

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

                val diffDays = calendarDaysBetween(startCal.timeInMillis, targetCal.timeInMillis)

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

            FrequencyType.CYCLIC -> {
                val activeDays = (medication.activeDays ?: 21).coerceAtLeast(1)
                val restDays = (medication.restDays ?: 7).coerceAtLeast(0)
                val cycleLength = activeDays + restDays
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = medication.startDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                var searchCal = targetCal.clone() as Calendar
                if (searchCal.timeInMillis <= fromMillis) {
                    searchCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                var attempts = 0
                while (attempts < cycleLength) {
                    val diffDays = calendarDaysBetween(startCal.timeInMillis, searchCal.timeInMillis)
                    val dayInCycle = if (diffDays >= 0) diffDays % cycleLength else cycleLength + (diffDays % cycleLength)
                    if (dayInCycle < activeDays) {
                        return searchCal.timeInMillis
                    }
                    searchCal.add(Calendar.DAY_OF_YEAR, 1)
                    attempts++
                }
                null
            }

            FrequencyType.AS_NEEDED -> null
        }
    }
}
