package com.gokcank.curalis.data.mapper

import com.gokcank.curalis.data.local.entity.StockHistoryEntity
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry

fun StockHistoryEntity.toDomain(): StockHistoryEntry {
    val parsedReason = try {
        StockChangeReason.valueOf(reason)
    } catch (e: Exception) {
        StockChangeReason.MANUAL_EDIT
    }
    return StockHistoryEntry(
        id = id,
        medicationId = medicationId,
        previousStock = previousStock,
        newStock = newStock,
        reason = parsedReason,
        timestamp = timestamp
    )
}

fun StockHistoryEntry.toEntity(): StockHistoryEntity {
    return StockHistoryEntity(
        id = id,
        medicationId = medicationId,
        previousStock = previousStock,
        newStock = newStock,
        reason = reason.name,
        timestamp = timestamp
    )
}
