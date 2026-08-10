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
    val formType: String = "PILL",
    val colorHex: String = "#1E88E5",
    val iconShape: String = "PILL",
    val dosage: String?,
    val unit: String?,
    val notes: String?,
    val mealInstruction: String = "DOES_NOT_MATTER",
    val expiryDate: Long? = null,
    val frequencyType: String = "DAILY", // DAILY, SPECIFIC_DAYS, INTERVAL, AS_NEEDED
    val intervalDays: Int? = null,
    val activeDays: Int? = null,
    val restDays: Int? = null,
    val startDate: Long = System.currentTimeMillis(),
    val initialStock: Int? = null,
    val currentStock: Int? = null,
    val refillThreshold: Int? = null,
    val isRefillReminderEnabled: Boolean = false,
    val isVerifiedSource: Boolean = false,
    val isArchived: Boolean = false
)
