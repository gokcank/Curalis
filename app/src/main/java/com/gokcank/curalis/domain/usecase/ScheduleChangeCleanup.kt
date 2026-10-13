package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import java.util.Calendar

/**
 * "Bugünden itibaren" uygulanan bir saat değişikliğinde silinmesi gereken, saati gelmiş ama
 * yanıtlanmamış bugünkü dozlar.
 *
 * Saat değişikliği gelecekteki dozları zaten yeniden üretiyor; ancak saati gelmiş (bildirimi
 * düşmüş) ve henüz alınmamış/atlanmamış bir doz geçmiş sayılıp bırakılıyordu. Örneğin 19:00
 * hatırlatması geldikten sonra saat 19:30'a alınınca 19:00 dozu "yanıt bekleniyor" olarak
 * kalıyor, tekrar hatırlatılıyor ve sonunda "kaçırıldı" sayılıyordu.
 *
 * Yalnızca plandan çıkarılan (ya da saati değiştirilen) saatlere ait dozlar seçilir: saati
 * değişmeyen dozlar ve ertelemeyle oluşmuş, plandaki hiçbir saate denk gelmeyen dozlar korunur.
 */
object ScheduleChangeCleanup {

    fun staleUnansweredDosesToday(
        reminders: List<Reminder>,
        oldTimes: List<MedicationTime>,
        newTimes: List<MedicationTime>,
        now: Long,
        startOfToday: Long
    ): List<Reminder> {
        val removedSlots = oldTimes.map { it.slot() }.toSet() - newTimes.map { it.slot() }.toSet()
        if (removedSlots.isEmpty()) return emptyList()
        return reminders.filter {
            (it.state == ReminderState.SCHEDULED || it.state == ReminderState.DELIVERED) &&
                it.timeInMillis in startOfToday until now &&
                slotOf(it.timeInMillis) in removedSlots
        }
    }

    private fun MedicationTime.slot() = hour * 60 + minute

    private fun slotOf(timeInMillis: Long): Int {
        val cal = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }
}
