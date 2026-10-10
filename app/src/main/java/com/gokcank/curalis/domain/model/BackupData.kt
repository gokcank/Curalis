package com.gokcank.curalis.domain.model

import com.gokcank.curalis.data.local.entity.AppointmentEntity
import com.gokcank.curalis.data.local.entity.DailyNoteEntity
import com.gokcank.curalis.data.local.entity.DoctorEntity
import com.gokcank.curalis.data.local.entity.EmergencyContactEntity
import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.data.local.entity.StockHistoryEntity
import com.gokcank.curalis.data.local.entity.SymptomEntity
import com.gokcank.curalis.data.local.entity.VitalEntity
import com.gokcank.curalis.data.local.entity.VitalReminderEntity

/**
 * Root data structure for exporting/importing the entire database as a JSON file.
 * This class holds lists of all entities in the database.
 *
 * The version-2 lists are nullable on purpose: Gson ignores Kotlin default values and
 * leaves a field null when the key is missing, which is exactly the case for version-1
 * backups. null means "this backup predates the field" — the restore then leaves that
 * table untouched instead of wiping it.
 */
data class BackupData(
    val version: Int = CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val medications: List<MedicationEntity> = emptyList(),
    val medicationDays: List<MedicationDaysEntity> = emptyList(),
    val medicationTimes: List<MedicationTimeEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val doctors: List<DoctorEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val vitals: List<VitalEntity> = emptyList(),
    val stockHistory: List<StockHistoryEntity>? = null,
    val emergencyContacts: List<EmergencyContactEntity>? = null,
    val symptoms: List<SymptomEntity>? = null,
    val vitalReminders: List<VitalReminderEntity>? = null,
    val dailyNotes: List<DailyNoteEntity>? = null
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}
