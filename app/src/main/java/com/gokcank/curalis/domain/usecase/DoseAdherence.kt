package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState

/**
 * Uyum yüzdesinin tek kuralı; ana ekran, istatistikler, takvim ve PDF raporu bunu kullanır.
 *
 * - Yalnızca sonuçlanmış (alınmış/atlanmış/kaçırılmış) dozlar sayılır; saati gelmemiş dozlar
 *   oranı haksız yere düşürmemeli. Plasebo dozları (bkz. Reminder.isPlacebo) hiç sayılmaz.
 * - Zamanında alınan doz tam, geç alınan doz yarım sayılır (ör. 4 dozdan 3'ü zamanında, 1'i geç
 *   → %87).
 */
object DoseAdherence {

    /**
     * Doz, hatırlatma zamanından [thresholdMinutes] dakikadan fazla sonra alındıysa geç sayılır.
     * Yeniden planlanmış dozda gecikme yeni zamana göre ölçülür. Alınma saati kaydedilmemiş
     * (eski sürümlerden kalan) dozlar zamanında sayılır.
     */
    fun isLate(reminder: Reminder, thresholdMinutes: Int): Boolean {
        if (reminder.state != ReminderState.TAKEN) return false
        val takenAt = reminder.takenAtMillis ?: return false
        val dueAt = reminder.rescheduledToMillis ?: reminder.timeInMillis
        return takenAt - dueAt > thresholdMinutes * 60_000L
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
}
