package com.gokcank.curalis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gokcank.curalis.data.local.dao.DrugDao
import com.gokcank.curalis.data.local.entity.DrugEntity

/**
 * TİTCK listesinden üretilen, salt okunur ilaç sözlüğü (assets/database/medications.db).
 *
 * Room bu dosyayı yalnızca cihazda hiç yoksa kopyalar; zaten kurulu uygulamalara yeni
 * listenin ulaşması için her liste güncellemesinde [VERSION] bir artırılmalı ve dosyanın
 * içindeki sürüm (PRAGMA user_version) de aynı değere ayarlanmalı. Sürüm farkını gören
 * Room eski kopyayı silip yenisini kopyalar (bkz. DatabaseModule'deki yıkıcı migration).
 * İkisinin eşleştiğini MedicationDictionaryAssetTest denetler.
 */
@Database(entities = [DrugEntity::class], version = MedicationDictionaryDatabase.VERSION, exportSchema = false)
abstract class MedicationDictionaryDatabase : RoomDatabase() {
    abstract val drugDao: DrugDao

    companion object {
        const val VERSION = 2
        const val DATABASE_NAME = "medications.db"
        const val DATABASE_DATE = "09.10.2026"
    }
}
