package com.gokcank.curalis.domain.model

import java.util.UUID

/** Bir ilacın stok miktarının neden/ne zaman değiştiğinin kalıcı kaydı. */
data class StockHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val previousStock: Int?,
    val newStock: Int?,
    val reason: StockChangeReason,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StockChangeReason {
    /** Bir doz alındı olarak işaretlendiğinde otomatik düşüş. */
    DOSE_TAKEN,
    /** Kullanıcı ilaç düzenleme ekranından stok sayısını elle değiştirdi. */
    MANUAL_EDIT,
    /** Kullanıcı yeni kutu/reçete girdiğinde stok artışı. */
    REFILL
}
