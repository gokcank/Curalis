package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.StockHistoryDao
import com.gokcank.curalis.data.mapper.toDomain
import com.gokcank.curalis.data.mapper.toEntity
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StockHistoryRepositoryImpl @Inject constructor(
    private val dao: StockHistoryDao
) : StockHistoryRepository {

    override suspend fun logChange(entry: StockHistoryEntry) {
        dao.insert(entry.toEntity())
    }

    override fun getHistoryForMedication(medicationId: String): Flow<List<StockHistoryEntry>> {
        return dao.getHistoryForMedication(medicationId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllHistory(): Flow<List<StockHistoryEntry>> {
        return dao.getAllHistory().map { entities -> entities.map { it.toDomain() } }
    }
}
