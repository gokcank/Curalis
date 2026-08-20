package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val medicationId: String,
    val timeInMillis: Long,
    val state: String,
    val skipReason: String? = null,
    val takenAtMillis: Long? = null,
    val isPlacebo: Boolean = false
)
