package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val severity: Int,
    val timeInMillis: Long,
    val notes: String?
)
