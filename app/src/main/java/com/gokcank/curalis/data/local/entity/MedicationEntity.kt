package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val barcode: String?,
    val activeIngredient: String?,
    val form: String?,
    val dosage: String?,
    val unit: String?,
    val notes: String?,
    val frequencyType: String = "DAILY", // DAILY, SPECIFIC_DAYS, INTERVAL, AS_NEEDED
    val intervalDays: Int? = null,
    val startDate: Long = System.currentTimeMillis()
)
