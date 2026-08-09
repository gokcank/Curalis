package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_history")
data class StockHistoryEntity(
    @PrimaryKey
    val id: String,
    val medicationId: String,
    val previousStock: Int?,
    val newStock: Int?,
    val reason: String,
    val timestamp: Long
)
