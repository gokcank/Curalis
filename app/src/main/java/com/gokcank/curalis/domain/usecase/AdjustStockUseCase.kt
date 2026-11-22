package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Stoğun elle değiştirilmesi: ilaç listesindeki "Bir kutu ekle" kısa yolu ve "Stok gir".
 * Her değişiklik stok geçmişine yazılır.
 */
class AdjustStockUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val stockHistoryRepository: StockHistoryRepository
) {

    /**
     * Stoğa bir kutu (ilacın başlangıç stoğu kadar) ekler ve "yenileme" olarak kaydeder.
     * Başlangıç stoğu yoksa ya da sıfırsa hiçbir şey yapmaz ve false döner.
     */
    suspend fun addBox(medicationId: String): Boolean {
        val medication = medicationRepository.getMedicationById(medicationId).firstOrNull() ?: return false
        val boxSize = medication.initialStock?.takeIf { it > 0 } ?: return false
        val previous = medication.currentStock ?: 0
        apply(medicationId, medication.currentStock, previous + boxSize, StockChangeReason.REFILL)
        return true
    }

    /** Stoğu verilen sayıya ayarlar; artışsa "yenileme", değilse "elle düzenleme" olarak kaydeder. */
    suspend fun setStock(medicationId: String, newStock: Int) {
        val medication = medicationRepository.getMedicationById(medicationId).firstOrNull() ?: return
        val stock = newStock.coerceAtLeast(0)
        if (medication.currentStock == stock) return
        apply(medicationId, medication.currentStock, stock, reasonFor(medication.currentStock, stock))
    }

    private suspend fun apply(medicationId: String, previous: Int?, new: Int, reason: StockChangeReason) {
        val medication = medicationRepository.getMedicationById(medicationId).firstOrNull() ?: return
        medicationRepository.updateMedication(medication.copy(currentStock = new))
        stockHistoryRepository.logChange(
            StockHistoryEntry(medicationId = medicationId, previousStock = previous, newStock = new, reason = reason)
        )
    }

    companion object {
        /** Elle girilen stok önceki değerden büyükse yeni kutu (yenileme), değilse düzeltmedir. */
        fun reasonFor(previous: Int?, new: Int?): StockChangeReason =
            if (new != null && previous != null && new > previous) StockChangeReason.REFILL else StockChangeReason.MANUAL_EDIT
    }
}
