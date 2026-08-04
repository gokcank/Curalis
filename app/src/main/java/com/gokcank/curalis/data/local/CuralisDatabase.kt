package com.gokcank.curalis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gokcank.curalis.data.local.dao.MedicationDao
import com.gokcank.curalis.data.local.dao.ReminderDao
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity

import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.data.local.entity.DoctorEntity
import com.gokcank.curalis.data.local.entity.AppointmentEntity
import com.gokcank.curalis.data.local.entity.VitalEntity

@Database(
    entities = [
        MedicationEntity::class, 
        ReminderEntity::class,
        MedicationDaysEntity::class,
        MedicationTimeEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class,
        VitalEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CuralisDatabase : RoomDatabase() {
    abstract val medicationDao: MedicationDao
    abstract val reminderDao: ReminderDao
    abstract val doctorDao: com.gokcank.curalis.data.local.dao.DoctorDao
    abstract val appointmentDao: com.gokcank.curalis.data.local.dao.AppointmentDao
    abstract val vitalDao: com.gokcank.curalis.data.local.dao.VitalDao
    abstract val backupDao: com.gokcank.curalis.data.local.dao.BackupDao
    
    companion object {
        const val DATABASE_NAME = "curalis_db"
        const val DATABASE_DATE = "24.07.2026"
    }
}
