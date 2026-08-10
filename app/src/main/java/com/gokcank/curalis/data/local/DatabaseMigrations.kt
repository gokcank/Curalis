package com.gokcank.curalis.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * CuralisDatabase için gerçek migration tanımları. fallbackToDestructiveMigration()
 * kasıtlı olarak kullanılmıyor (bkz. DatabaseModule) — her yeni sürüm burada elle
 * tanımlanmalı ki kullanıcı verisi güncellemelerde kaybolmasın.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // "Doğrulanmış kaynak / kullanıcı girişi" ayrımı için yeni sütun (bkz. Medication.isVerifiedSource).
        db.execSQL("ALTER TABLE medications ADD COLUMN isVerifiedSource INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Stok geçmişi (bkz. StockHistoryEntry) — daha önce stok her değişimde üzerine
        // yazılıyor, hiçbir kayıt tutulmuyordu.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS stock_history (
                id TEXT NOT NULL PRIMARY KEY,
                medicationId TEXT NOT NULL,
                previousStock INTEGER,
                newStock INTEGER,
                reason TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // İlaç atlama sebebi (bkz. SkipReason) — daha önce yalnızca durum tutuluyordu, sebep hiç kaydedilmiyordu.
        db.execSQL("ALTER TABLE reminders ADD COLUMN skipReason TEXT")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // İlaç silinirken geçmiş doz kayıtlarının korunabilmesi için "arşivle" seçeneği
        // eklendi — arşivlenen ilaç satırı gerçekten silinmez, yalnızca aktif listelerden
        // gizlenir (bkz. Medication.isArchived).
        db.execSQL("ALTER TABLE medications ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
    }
}

val CURALIS_MIGRATIONS = arrayOf(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
