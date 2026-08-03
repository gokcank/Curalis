package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vitals")
data class VitalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String, // e.g. "BloodPressure", "BloodSugar", "HeartRate", "Weight", "Temperature"
    val value1: Double, // Main value (e.g. Systolic, or Glucose)
    val value2: Double?, // Secondary value (e.g. Diastolic for BP)
    val unit: String, // e.g. "mmHg", "mg/dL", "bpm", "kg", "°C"
    val timeInMillis: Long,
    val notes: String?
)
