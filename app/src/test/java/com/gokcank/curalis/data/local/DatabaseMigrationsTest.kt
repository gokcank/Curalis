package com.gokcank.curalis.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * Upgrades real databases built from the exported schemas (app/schemas) through
 * [CURALIS_MIGRATIONS], the same way an installed app would: the file is created at an old
 * version and then opened by Room, which runs the migrations and validates every table
 * against the current entities (throwing "Migration didn't properly handle ..." on any
 * mismatch). Rows written at the oldest version must survive the whole chain.
 *
 * Room's MigrationTestHelper is not used because it can only read schemas from the app's
 * assets, which would mean shipping them inside the debug APK.
 */
@RunWith(RobolectricTestRunner::class)
class DatabaseMigrationsTest {

    private val oldestVersion = 7
    private val latestVersion = 23
    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `migration chain has no gaps`() {
        val steps = CURALIS_MIGRATIONS.map { it.startVersion to it.endVersion }
        assertEquals((oldestVersion until latestVersion).map { it to it + 1 }, steps)
    }

    @Test
    fun `latest exported schema matches the database version`() {
        assertEquals(latestVersion, schemaFile(latestVersion).let { readVersion(it) })
        assertEquals(false, schemaFile(latestVersion + 1).exists())
    }

    @Test
    fun `every version upgrades to the current entities`() {
        for (version in oldestVersion until latestVersion) {
            val name = "migration-from-$version"
            createDatabaseAtVersion(name, version).close()
            withRoom(name) { db ->
                // Any query forces Room to open the file, migrate and validate it.
                runBlocking { db.medicationDao.getMedicationById("none").first() }
            }
        }
    }

    @Test
    fun `data written at the oldest version survives with defaults for new columns`() {
        val name = "migration-data"
        createDatabaseAtVersion(name, oldestVersion).use { seedVersion7(it) }

        withRoom(name) { room ->
            val db = room.openHelper.writableDatabase
            db.query(
                "SELECT name, activeIngredient, currentStock, isVerifiedSource, isArchived, isSuspended, " +
                    "hasPlaceboDays, photoPath, doctorId, timesEffectiveFrom FROM medications WHERE id = 'm1'"
            ).use { c ->
                c.moveToFirst()
                assertEquals("Parol", c.getString(0))
                assertEquals("parasetamol", c.getString(1))
                assertEquals(20, c.getInt(2))
                assertEquals(0, c.getInt(3))
                assertEquals(0, c.getInt(4))
                assertEquals(0, c.getInt(5))
                assertEquals(0, c.getInt(6))
                assertNull(c.getString(7))
                assertNull(c.getString(8))
                assertNull(c.getString(9))
            }
            db.query("SELECT state, skipReason, takenAtMillis, isPlacebo FROM reminders WHERE id = 'r1'").use { c ->
                c.moveToFirst()
                assertEquals("TAKEN", c.getString(0))
                assertNull(c.getString(1))
                assertNull(c.getString(2))
                assertEquals(0, c.getInt(3))
            }
            db.query("SELECT doctorId, emergencyContactId, isVisited, visitNote FROM appointments WHERE id = 'a1'").use { c ->
                c.moveToFirst()
                assertEquals("d1", c.getString(0))
                assertNull(c.getString(1))
                assertEquals(0, c.getInt(2))
                assertNull(c.getString(3))
            }
            for (table in listOf("medication_times", "medication_days", "doctors", "vitals")) {
                db.query("SELECT COUNT(*) FROM $table").use { c ->
                    c.moveToFirst()
                    assertEquals(table, 1, c.getInt(0))
                }
            }
            assertEquals("Parol", runBlocking { room.medicationDao.getMedicationById("m1").first() }?.name)
        }
    }

    private fun schemaFile(version: Int) =
        File("schemas/${CuralisDatabase::class.java.name}/$version.json")

    private fun readVersion(file: File): Int =
        JsonParser.parseString(file.readText()).asJsonObject["database"].asJsonObject["version"].asInt

    /** Builds the database file exactly as Room exported it for [version]. */
    private fun createDatabaseAtVersion(name: String, version: Int): SQLiteDatabase {
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        file.delete()
        val schema = JsonParser.parseString(schemaFile(version).readText()).asJsonObject["database"].asJsonObject
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        for (entity in schema["entities"].asJsonArray.map { it.asJsonObject }) {
            val table = entity["tableName"].asString
            db.execSQL(entity["createSql"].asString.replace("\${TABLE_NAME}", table))
            entity["indices"]?.asJsonArray?.forEach { index ->
                db.execSQL(index.asJsonObject["createSql"].asString.replace("\${TABLE_NAME}", table))
            }
        }
        schema["setupQueries"].asJsonArray.forEach { db.execSQL(it.asString) }
        db.version = version
        return db
    }

    private fun withRoom(name: String, block: (CuralisDatabase) -> Unit) {
        val db = Room.databaseBuilder(context, CuralisDatabase::class.java, name)
            .addMigrations(*CURALIS_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            block(db)
        } finally {
            db.close()
        }
    }

    private fun seedVersion7(db: SQLiteDatabase) {
        db.execSQL(
            "INSERT INTO medications (id, name, barcode, activeIngredient, form, formType, colorHex, iconShape, " +
                "dosage, unit, notes, mealInstruction, expiryDate, frequencyType, intervalDays, activeDays, " +
                "restDays, startDate, initialStock, currentStock, refillThreshold, isRefillReminderEnabled) " +
                "VALUES ('m1', 'Parol', NULL, 'parasetamol', NULL, 'PILL', '#1E88E5', 'PILL', '500', 'mg', NULL, " +
                "'DOES_NOT_MATTER', NULL, 'DAILY', NULL, NULL, NULL, 1000, 20, 20, 5, 1)"
        )
        db.execSQL("INSERT INTO medication_times (id, medicationId, hour, minute, dose) VALUES ('t1', 'm1', 9, 0, NULL)")
        db.execSQL("INSERT INTO medication_days (medicationId, dayOfWeek) VALUES ('m1', 1)")
        db.execSQL("INSERT INTO reminders (id, medicationId, timeInMillis, state) VALUES ('r1', 'm1', 2000, 'TAKEN')")
        db.execSQL(
            "INSERT INTO doctors (id, name, specialty, phoneNumber, email, hospitalName, notes) " +
                "VALUES ('d1', 'Dr. Test', NULL, NULL, NULL, NULL, NULL)"
        )
        db.execSQL(
            "INSERT INTO appointments (id, doctorId, title, timeInMillis, location, notes) " +
                "VALUES ('a1', 'd1', 'Kontrol', 3000, NULL, NULL)"
        )
        db.execSQL(
            "INSERT INTO vitals (id, type, value1, value2, unit, timeInMillis, notes) " +
                "VALUES ('v1', 'WEIGHT', 70.0, NULL, 'kg', 4000, NULL)"
        )
    }
}
