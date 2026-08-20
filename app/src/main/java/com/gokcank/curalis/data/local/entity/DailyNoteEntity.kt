package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_notes")
data class DailyNoteEntity(
    @PrimaryKey
    val dateMillis: Long,
    val content: String,
    val mood: String?
)
