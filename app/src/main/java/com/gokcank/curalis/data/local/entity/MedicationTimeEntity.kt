package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "medication_times",
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
data class MedicationTimeEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val hour: Int,
    val minute: Int,
    val dose: String? = null // e.g. "1 pill", "2 drops"
)
