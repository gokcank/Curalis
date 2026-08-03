package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "medication_days",
    primaryKeys = ["medicationId", "dayOfWeek"],
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
data class MedicationDaysEntity(
    val medicationId: String,
    val dayOfWeek: Int // 1 = Monday, 7 = Sunday (ISO-8601 standard)
)
