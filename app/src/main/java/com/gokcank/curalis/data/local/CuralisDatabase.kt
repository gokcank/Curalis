package com.gokcank.curalis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gokcank.curalis.data.local.dao.MedicationDao
import com.gokcank.curalis.data.local.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CuralisDatabase : RoomDatabase() {
    abstract val medicationDao: MedicationDao
    
    companion object {
        const val DATABASE_NAME = "curalis_db"
    }
}
