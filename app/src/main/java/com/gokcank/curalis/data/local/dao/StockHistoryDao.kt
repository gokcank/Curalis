package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.StockHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockHistoryDao {

    @Insert
    suspend fun insert(entry: StockHistoryEntity)

    @Query("SELECT * FROM stock_history WHERE medicationId = :medicationId ORDER BY timestamp DESC")
    fun getHistoryForMedication(medicationId: String): Flow<List<StockHistoryEntity>>
}
