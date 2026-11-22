package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.LowStockNotifier
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Bir doz "alındı" olunca stoktan kaç adet düşüleceği.
 *
 * Dozun planlandığı saatin ilaçtaki karşılığına yazılmış doz düz bir tam sayıysa (ör. "2")
 * o kadar düşülür. Kesirli ("1,5"), metin ("yarım") ya da boş dozlarda 1 düşülür — stok
 * tam sayı olarak tutulduğu için kesir güvenle düşülemez.
 */
object IntakeStockAmount {
    const val DEFAULT_AMOUNT = 1

    fun forReminder(medication: Medication, reminder: Reminder, zone: ZoneId = ZoneId.systemDefault()): Int {
        val planned = Instant.ofEpochMilli(reminder.timeInMillis).atZone(zone)
        val dose = medication.times
            .firstOrNull { it.hour == planned.hour && it.minute == planned.minute }
            ?.dose
        return parse(dose)
    }

    fun parse(dose: String?): Int {
        val value = dose?.trim()?.toIntOrNull() ?: return DEFAULT_AMOUNT
        return if (value > 0) value else DEFAULT_AMOUNT
    }
}

/**
 * Bir dozun durumu değiştiğinde stoğu günceller. Doz hangi yoldan işaretlenirse işaretlensin
 * (bildirim, tam ekran, zaman çizelgesi, ana ekran, widget) aynı kural uygulanır:
 * - "alındı" olmayan bir doz "alındı" olunca stok düşer, eşiğin altına inerse uyarı verilir;
 * - "alındı" olan bir doz başka bir duruma dönünce (geri alma) aynı miktar iade edilir.
 * Stok takibi kapalıysa (stok girilmemişse) hiçbir şey yapılmaz.
 */
class ApplyIntakeStockChangeUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val stockHistoryRepository: StockHistoryRepository,
    private val lowStockNotifier: LowStockNotifier
) {
    suspend operator fun invoke(reminder: Reminder, previousState: ReminderState?, newState: ReminderState) {
        val wasTaken = previousState == ReminderState.TAKEN
        val isTaken = newState == ReminderState.TAKEN
        if (wasTaken == isTaken) return

        val medication = medicationRepository.getMedicationById(reminder.medicationId).firstOrNull() ?: return
        val currentStock = medication.currentStock ?: return
        val amount = IntakeStockAmount.forReminder(medication, reminder)

        if (isTaken) {
            if (currentStock <= 0) return
            val newStock = (currentStock - amount).coerceAtLeast(0)
            saveStock(medication, currentStock, newStock, StockChangeReason.DOSE_TAKEN)
            if (medication.isRefillReminderEnabled && newStock <= (medication.refillThreshold ?: DEFAULT_REFILL_THRESHOLD)) {
                lowStockNotifier.notifyLowStock(medication.name, newStock)
            }
        } else {
            saveStock(medication, currentStock, currentStock + amount, StockChangeReason.DOSE_UNDONE)
        }
    }

    private suspend fun saveStock(medication: Medication, previous: Int, newStock: Int, reason: StockChangeReason) {
        medicationRepository.updateMedication(medication.copy(currentStock = newStock))
        stockHistoryRepository.logChange(
            StockHistoryEntry(
                medicationId = medication.id,
                previousStock = previous,
                newStock = newStock,
                reason = reason
            )
        )
    }

    companion object {
        /** İlaçta eşik girilmemişse kullanılan varsayılan; listedeki "stok az" etiketiyle aynı. */
        const val DEFAULT_REFILL_THRESHOLD = 5
    }
}
