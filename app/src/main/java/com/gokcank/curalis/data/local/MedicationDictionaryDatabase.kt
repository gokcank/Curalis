package com.gokcank.curalis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gokcank.curalis.data.local.dao.DrugDao
import com.gokcank.curalis.data.local.entity.DrugEntity

@Database(entities = [DrugEntity::class], version = 1, exportSchema = false)
abstract class MedicationDictionaryDatabase : RoomDatabase() {
    abstract val drugDao: DrugDao

    companion object {
        const val DATABASE_NAME = "medications.db"
        const val DATABASE_DATE = "07.08.2026"
    }
}
