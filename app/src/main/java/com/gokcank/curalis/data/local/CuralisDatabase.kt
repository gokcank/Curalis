package com.gokcank.curalis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gokcank.curalis.data.local.dao.MedicationDao
import com.gokcank.curalis.data.local.dao.ReminderDao
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity

@Database(
    entities = [MedicationEntity::class, ReminderEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CuralisDatabase : RoomDatabase() {
    abstract val medicationDao: MedicationDao
    abstract val reminderDao: ReminderDao
    
    companion object {
        const val DATABASE_NAME = "curalis_db"
    }
}
