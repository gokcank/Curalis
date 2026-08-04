package com.gokcank.curalis.domain.model

import com.gokcank.curalis.data.local.entity.AppointmentEntity
import com.gokcank.curalis.data.local.entity.DoctorEntity
import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.data.local.entity.VitalEntity

/**
 * Root data structure for exporting/importing the entire database as a JSON file.
 * This class holds lists of all entities in the database.
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val medications: List<MedicationEntity> = emptyList(),
    val medicationDays: List<MedicationDaysEntity> = emptyList(),
    val medicationTimes: List<MedicationTimeEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val doctors: List<DoctorEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val vitals: List<VitalEntity> = emptyList()
)
