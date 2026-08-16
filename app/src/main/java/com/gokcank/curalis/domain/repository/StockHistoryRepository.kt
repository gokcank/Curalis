package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.StockHistoryEntry
import kotlinx.coroutines.flow.Flow

interface StockHistoryRepository {
    suspend fun logChange(entry: StockHistoryEntry)
    fun getHistoryForMedication(medicationId: String): Flow<List<StockHistoryEntry>>
    fun getAllHistory(): Flow<List<StockHistoryEntry>>
}
