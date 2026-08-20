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

    /**
     * Bir ilacın belirli bir günde alınması gerekip gerekmediğini, sıklık tipine göre hesaplar.
     * Bu, tek bir sonraki tetiklenmeyi bulan [calculateNextTriggerTime] ile bir tarih aralığındaki
     * tüm dozları listeleyen [GetRemindersBetweenDatesUseCase] arasında paylaşılan tek kaynaktır;
     * böylece iki yerde birbirinden bağımsız, zamanla farklılaşabilecek gün-eşleştirme mantığı olmaz.
     */
    fun shouldTakeOnDay(medication: Medication, dayMillis: Long): Boolean {
        if (medication.frequencyType == FrequencyType.AS_NEEDED) return false

        val dayStartCal = Calendar.getInstance().apply {
            timeInMillis = dayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val medStartCal = Calendar.getInstance().apply {
            timeInMillis = medication.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (dayStartCal.timeInMillis < medStartCal.timeInMillis) return false

        return when (medication.frequencyType) {
            FrequencyType.DAILY -> true

            FrequencyType.INTERVAL -> {
                val interval = (medication.intervalDays ?: 2).coerceAtLeast(1)
                val diffDays = calendarDaysBetween(medStartCal.timeInMillis, dayStartCal.timeInMillis)
                diffDays >= 0 && diffDays % interval == 0
            }

            FrequencyType.SPECIFIC_DAYS -> {
                val specificDays = medication.specificDays
                if (specificDays.isEmpty()) {
                    true
                } else {
                    fun calendarDayToIso(calDay: Int) = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
                    specificDays.contains(calendarDayToIso(dayStartCal.get(Calendar.DAY_OF_WEEK)))
                }
            }

            FrequencyType.CYCLIC -> {
                val activeDays = (medication.activeDays ?: 21).coerceAtLeast(1)
                val restDays = (medication.restDays ?: 7).coerceAtLeast(0)
                val cycleLength = activeDays + restDays
                val diffDays = calendarDaysBetween(medStartCal.timeInMillis, dayStartCal.timeInMillis)
                if (diffDays < 0) {
                    false
                } else if (medication.hasPlaceboDays) {
                    // Plasebo hap açıksa dinlenme günlerinde de bir hatırlatıcı üretilir;
                    // hangi günlerin plasebo olduğu isPlaceboDay ile ayrıca işaretlenir.
                    true
                } else {
                    (diffDays % cycleLength) < activeDays
                }
            }

            FrequencyType.AS_NEEDED -> false
        }
    }

    /**
     * Bir CYCLIC ilacın verilen günde üretilecek dozunun plasebo (dinlenme günü) hapı olup
     * olmadığını belirler. [shouldTakeOnDay] ile aynı gün-eşleştirme mantığını (medStartCal
     * hizalaması) tekrarlar ki iki fonksiyon zamanla birbirinden sapmasın.
     */
    fun isPlaceboDay(medication: Medication, dayMillis: Long): Boolean {
        if (medication.frequencyType != FrequencyType.CYCLIC || !medication.hasPlaceboDays) return false

        val dayStartCal = Calendar.getInstance().apply {
            timeInMillis = dayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val medStartCal = Calendar.getInstance().apply {
            timeInMillis = medication.startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (dayStartCal.timeInMillis < medStartCal.timeInMillis) return false

        val activeDays = (medication.activeDays ?: 21).coerceAtLeast(1)
        val restDays = (medication.restDays ?: 7).coerceAtLeast(0)
        val cycleLength = activeDays + restDays
        val diffDays = calendarDaysBetween(medStartCal.timeInMillis, dayStartCal.timeInMillis)
        return diffDays >= 0 && (diffDays % cycleLength) >= activeDays
    }

    /**
     * Aranacak maksimum gün sayısı, sıklık tipinin döngü uzunluğuna göre belirlenir
     * (ör. INTERVAL için aralık uzunluğu, CYCLIC için tam döngü uzunluğu) — bu kadar gün
     * içinde uygun bir gün bulunamazsa döngüde bir hata var demektir.
     */
    private fun maxSearchDays(medication: Medication): Int = when (medication.frequencyType) {
        FrequencyType.DAILY -> 1
        FrequencyType.INTERVAL -> (medication.intervalDays ?: 2).coerceAtLeast(1)
        FrequencyType.SPECIFIC_DAYS -> 8
        FrequencyType.CYCLIC -> (medication.activeDays ?: 21).coerceAtLeast(1) + (medication.restDays ?: 7).coerceAtLeast(0)
        FrequencyType.AS_NEEDED -> 0
    }

    fun calculateNextTriggerTime(
        medication: Medication,
        medTime: MedicationTime,
        fromMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (medication.frequencyType == FrequencyType.AS_NEEDED) return null

        val dayCal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        repeat(maxSearchDays(medication) + 1) {
            if (shouldTakeOnDay(medication, dayCal.timeInMillis)) {
                val triggerCal = (dayCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, medTime.hour)
                    set(Calendar.MINUTE, medTime.minute)
                }
                if (triggerCal.timeInMillis > fromMillis) {
                    return triggerCal.timeInMillis
                }
            }
            dayCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }
}
