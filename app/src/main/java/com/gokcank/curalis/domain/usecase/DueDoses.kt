package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState

/**
 * Tam ekran hatırlatmada listelenen dozlar: bugün hatırlatma zamanı gelmiş ve henüz
 * yanıtlanmamış (alınmamış, atlanmamış, ertelenmemiş) tüm dozlar. Yalnızca o an çalan doz
 * değil; gün içinde yanıtsız kalmış dozlar da listelenir ki unutulan bir doz gözden kaçmasın.
 *
 * Hatırlatma zamanı, doz yeniden planlandıysa yeni zamanıdır. Ertelenen bir dozun yerine
 * oluşturulan yeni kayıt da, zamanı gelince bu listeye girer. En yeni (şu an çalan) dozlar
 * önce gelir; gün içinde yanıtsız kalanlar altta listelenir.
 */
object DueDoses {

    /** Hatırlatma zamanı birkaç saniye sonraya denk gelen dozlar da (aynı dakika) dahil edilir. */
    private const val GRACE_MILLIS = 60_000L

    fun unansweredToday(
        reminders: List<Reminder>,
        activeMedicationIds: Set<String>,
        now: Long,
        startOfToday: Long
    ): List<Reminder> = reminders
        .filter { it.medicationId in activeMedicationIds }
        .filter { it.state == ReminderState.SCHEDULED || it.state == ReminderState.DELIVERED }
        .filter { it.reminderTime() in startOfToday..(now + GRACE_MILLIS) }
        .sortedByDescending { it.reminderTime() }

    private fun Reminder.reminderTime() = rescheduledToMillis ?: timeInMillis
}
