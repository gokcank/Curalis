package com.gokcank.curalis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gokcank.curalis.data.local.dao.AppointmentDao
import com.gokcank.curalis.data.local.dao.BackupDao
import com.gokcank.curalis.data.local.dao.DoctorDao
import com.gokcank.curalis.data.local.dao.MedicationDao
import com.gokcank.curalis.data.local.dao.ReminderDao
import com.gokcank.curalis.data.local.dao.StockHistoryDao
import com.gokcank.curalis.data.local.dao.VitalDao
import com.gokcank.curalis.data.local.entity.AppointmentEntity
import com.gokcank.curalis.data.local.entity.DoctorEntity
import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.data.local.entity.StockHistoryEntity
import com.gokcank.curalis.data.local.entity.VitalEntity

@Database(
    entities = [
        MedicationEntity::class,
        ReminderEntity::class,
        MedicationDaysEntity::class,
        MedicationTimeEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class,
        VitalEntity::class,
        StockHistoryEntity::class
    ],
    version = 9,
    exportSchema = true
)
abstract class CuralisDatabase : RoomDatabase() {
    abstract val medicationDao: MedicationDao
    abstract val reminderDao: ReminderDao
    abstract val doctorDao: DoctorDao
    abstract val appointmentDao: AppointmentDao
    abstract val vitalDao: VitalDao
    abstract val backupDao: BackupDao
    abstract val stockHistoryDao: StockHistoryDao
    
    companion object {
        const val DATABASE_NAME = "curalis_db"
        const val DATABASE_DATE = "07.08.2026"
    }
}
