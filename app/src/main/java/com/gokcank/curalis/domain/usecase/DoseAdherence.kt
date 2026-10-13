package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import kotlin.math.roundToInt

/**
 * Uyum yüzdesinin tek kuralı; ana ekran, istatistikler, takvim ve PDF raporu bunu kullanır.
 *
 * - Yalnızca sonuçlanmış (alınmış/atlanmış/kaçırılmış) dozlar sayılır; saati gelmemiş dozlar
 *   oranı haksız yere düşürmemeli. Plasebo dozları (bkz. Reminder.isPlacebo) hiç sayılmaz.
 * - Zamanında alınan doz tam, geç alınan doz yarım sayılır (ör. 4 dozdan 3'ü zamanında, 1'i geç
 *   → %87).
 */
object DoseAdherence {

    /** Bir doz kümesinin (ör. bir ilacın rapor aralığındaki dozları) özeti. */
    data class Summary(
        val taken: Int,
        /** [taken] içindeki geç alınanlar. */
        val late: Int,
        val skipped: Int,
        val missed: Int,
        val percentage: Int?,
        /** Alınma saati kayıtlı dozların hatırlatma zamanından ortalama ne kadar sonra alındığı
         *  (erken alınan doz 0 sayılır). Alınma saati kayıtlı doz yoksa null. */
        val averageDelayMinutes: Int?
    ) {
        val total: Int get() = taken + skipped + missed
    }

    /**
     * Doz, hatırlatma zamanından [thresholdMinutes] dakikadan fazla sonra alındıysa geç sayılır.
     * Yeniden planlanmış dozda gecikme yeni zamana göre ölçülür. Alınma saati kaydedilmemiş
     * (eski sürümlerden kalan) dozlar zamanında sayılır.
     */
    fun isLate(reminder: Reminder, thresholdMinutes: Int): Boolean {
        if (reminder.state != ReminderState.TAKEN) return false
        val takenAt = reminder.takenAtMillis ?: return false
        return takenAt - dueAt(reminder) > thresholdMinutes * 60_000L
    }

    fun isResolved(state: ReminderState): Boolean =
        state == ReminderState.TAKEN || state == ReminderState.SKIPPED || state == ReminderState.MISSED

    /** Değerlendirilecek doz yoksa null; "veri yok" ile "%0 uyum" farklı şeylerdir. */
    fun percentage(reminders: List<Reminder>, thresholdMinutes: Int): Int? {
        val resolved = reminders.filter { !it.isPlacebo && isResolved(it.state) }
        if (resolved.isEmpty()) return null
        val taken = resolved.filter { it.state == ReminderState.TAKEN }
        val late = taken.count { isLate(it, thresholdMinutes) }
        val onTime = taken.size - late
        // Yarım doz kesirli olmasın diye yarım puanlarla hesaplanır.
        return (onTime * 2 + late) * 100 / (resolved.size * 2)
    }

    fun summarize(reminders: List<Reminder>, thresholdMinutes: Int): Summary {
        val counted = reminders.filter { !it.isPlacebo }
        val taken = counted.filter { it.state == ReminderState.TAKEN }
        val delays = taken.mapNotNull { dose ->
            dose.takenAtMillis?.let { (it - dueAt(dose)).coerceAtLeast(0L) }
        }
        return Summary(
            taken = taken.size,
            late = taken.count { isLate(it, thresholdMinutes) },
            skipped = counted.count { it.state == ReminderState.SKIPPED },
            missed = counted.count { it.state == ReminderState.MISSED },
            percentage = percentage(counted, thresholdMinutes),
            averageDelayMinutes = if (delays.isEmpty()) null else (delays.average() / 60_000L).roundToInt()
        )
    }

    private fun dueAt(reminder: Reminder) = reminder.rescheduledToMillis ?: reminder.timeInMillis
}
