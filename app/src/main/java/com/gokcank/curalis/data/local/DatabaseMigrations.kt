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

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // İlaç fotoğrafı — cihazda saklanan bir dosyanın yolu (bkz. MedicationPhotoStorage).
        // Sunucuya hiç yüklenmiyor.
        db.execSQL("ALTER TABLE medications ADD COLUMN photoPath TEXT")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Doktor–ilaç bağlantısı: bir ilacı isteğe bağlı olarak bir doktora bağlar
        // (bkz. AddEditMedicationScreen'deki doktor seçici ve AddEditDoctorScreen'deki
        // "bu doktora bağlı ilaçlar" listesi).
        db.execSQL("ALTER TABLE medications ADD COLUMN doctorId TEXT")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Randevu geçtikten sonra "gidildi mi" işareti + kısa bir ziyaret notu
        // (bkz. AddEditAppointmentScreen'deki "Ziyaret Bilgisi" bölümü).
        db.execSQL("ALTER TABLE appointments ADD COLUMN isVisited INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE appointments ADD COLUMN visitNote TEXT")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Doz zaman damgası: kullanıcının bir dozu gerçekte ne zaman aldığını belirtebilmesi
        // (Şimdi / Tam zamanında / elle seçilen bir saat) için (bkz. DoseTakenTimeDialog).
        db.execSQL("ALTER TABLE reminders ADD COLUMN takenAtMillis INTEGER")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Askıya Alma: arşivden farklı, geçici duraklatma (bkz. Medication.isSuspended).
        db.execSQL("ALTER TABLE medications ADD COLUMN isSuspended INTEGER NOT NULL DEFAULT 0")
        // Tedavi süresi (gün) — kutunun son kullanma tarihinden ayrı, yalnızca
        // bilgilendirme amaçlı bir alan (bkz. Medication.treatmentDurationDays).
        db.execSQL("ALTER TABLE medications ADD COLUMN treatmentDurationDays INTEGER")
    }
}

val CURALIS_MIGRATIONS = arrayOf(
    MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16
)
