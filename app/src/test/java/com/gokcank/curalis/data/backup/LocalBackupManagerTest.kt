package com.gokcank.curalis.data.backup

import androidx.room.Room
import com.gokcank.curalis.data.local.CuralisDatabase
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
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Backup round trips against a real (in-memory) Room database: every one of the 12 tables
 * must survive export → import, and version-1 backups (which predate five of them) must
 * still restore without wiping data they never contained.
 */
@RunWith(RobolectricTestRunner::class)
class LocalBackupManagerTest {

    private val gson = Gson()
    private lateinit var source: CuralisDatabase
    private lateinit var target: CuralisDatabase

    private fun newDatabase(): CuralisDatabase =
        Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), CuralisDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Before
    fun setUp() {
        source = newDatabase()
        target = newDatabase()
    }

    @After
    fun tearDown() {
        source.close()
        target.close()
    }

    private suspend fun seedOneOfEverything(db: CuralisDatabase) {
        val dao = db.backupDao
        dao.insertEmergencyContacts(listOf(EmergencyContactEntity(id = "c1", name = "Ayşe", relationship = "Kardeş", phoneNumber = "555", notes = null)))
        dao.insertDoctors(listOf(DoctorEntity(id = "d1", name = "Dr. Test", specialty = null, phoneNumber = null, email = null, hospitalName = null, notes = null)))
        dao.insertMedications(listOf(MedicationEntity(id = "m1", name = "Parol", barcode = null, activeIngredient = "parasetamol", form = null, dosage = null, unit = null, notes = null)))
        dao.insertMedicationTimes(listOf(MedicationTimeEntity(id = "t1", medicationId = "m1", hour = 9, minute = 0)))
        dao.insertMedicationDays(listOf(MedicationDaysEntity(medicationId = "m1", dayOfWeek = 1)))
        dao.insertReminders(listOf(ReminderEntity(id = "m1_1", medicationId = "m1", timeInMillis = 1L, state = "TAKEN")))
        dao.insertAppointments(listOf(AppointmentEntity(id = "a1", doctorId = "d1", emergencyContactId = "c1", title = "Kontrol", timeInMillis = 2L, location = null, notes = null)))
        dao.insertVitals(listOf(VitalEntity(id = "v1", type = "WEIGHT", value1 = 70.0, value2 = null, unit = "kg", timeInMillis = 3L, notes = null)))
        dao.insertStockHistory(listOf(StockHistoryEntity(id = "s1", medicationId = "m1", previousStock = 10, newStock = 9, reason = "DOSE_TAKEN", timestamp = 4L)))
        dao.insertSymptoms(listOf(SymptomEntity(id = "sy1", type = "PAIN", severity = 3, timeInMillis = 5L, notes = null)))
        dao.insertVitalReminders(listOf(VitalReminderEntity(type = "WEIGHT", enabled = true, hour = 8, minute = 30, daysOfWeek = "")))
        dao.insertDailyNotes(listOf(DailyNoteEntity(dateMillis = 6L, content = "İyi bir gün", mood = null)))
    }

    @Test
    fun `round trip restores all twelve tables`() = runBlocking {
        seedOneOfEverything(source)
        val json = LocalBackupManager(source, gson).exportData()

        val restored = LocalBackupManager(target, gson).importData(json)

        // medication, doctor, appointment, vital, contact, symptom, daily note
        assertEquals(7, restored)
        val dao = target.backupDao
        assertEquals(1, dao.getAllMedications().size)
        assertEquals(1, dao.getAllMedicationTimes().size)
        assertEquals(1, dao.getAllMedicationDays().size)
        assertEquals(1, dao.getAllReminders().size)
        assertEquals(1, dao.getAllDoctors().size)
        assertEquals("c1", dao.getAllAppointments().single().emergencyContactId)
        assertEquals(1, dao.getAllVitals().size)
        assertEquals(1, dao.getAllStockHistory().size)
        assertEquals(1, dao.getAllEmergencyContacts().size)
        assertEquals(1, dao.getAllSymptoms().size)
        assertEquals(1, dao.getAllVitalReminders().size)
        assertEquals("İyi bir gün", dao.getAllDailyNotes().single().content)
    }

    @Test
    fun `restore replaces existing data instead of merging`() = runBlocking {
        seedOneOfEverything(source)
        val json = LocalBackupManager(source, gson).exportData()
        target.backupDao.insertSymptoms(listOf(SymptomEntity(id = "old", type = "NAUSEA", severity = 1, timeInMillis = 9L, notes = null)))
        target.backupDao.insertDoctors(listOf(DoctorEntity(id = "oldDoctor", name = "Eski", specialty = null, phoneNumber = null, email = null, hospitalName = null, notes = null)))

        LocalBackupManager(target, gson).importData(json)

        assertEquals(listOf("sy1"), target.backupDao.getAllSymptoms().map { it.id })
        assertEquals(listOf("d1"), target.backupDao.getAllDoctors().map { it.id })
    }

    @Test
    fun `version 1 backup restores and keeps data types it never contained`() = runBlocking {
        seedOneOfEverything(source)
        // A version-1 backup has none of the five newer keys at all.
        val v1 = JsonParser.parseString(LocalBackupManager(source, gson).exportData()).asJsonObject.apply {
            listOf("stockHistory", "emergencyContacts", "symptoms", "vitalReminders", "dailyNotes").forEach { remove(it) }
            addProperty("version", 1)
        }.toString()
        target.backupDao.insertDailyNotes(listOf(DailyNoteEntity(dateMillis = 7L, content = "Hedefteki not", mood = null)))

        val restored = LocalBackupManager(target, gson).importData(v1)

        assertEquals(4, restored) // medication, doctor, appointment, vital
        assertEquals(listOf("Hedefteki not"), target.backupDao.getAllDailyNotes().map { it.content })
        // The appointment pointed at a contact this backup does not carry: the restore must
        // still succeed, with the dangling reference cleared rather than failing on the key.
        val appointment = target.backupDao.getAllAppointments().single()
        assertNull(appointment.emergencyContactId)
        assertEquals("d1", appointment.doctorId)
    }

    @Test
    fun `malformed backup fails without touching existing data`() = runBlocking {
        seedOneOfEverything(target)

        val restored = LocalBackupManager(target, gson).importData("{ this is not json")

        assertNull(restored)
        assertEquals(1, target.backupDao.getAllMedications().size)
        assertEquals(1, target.backupDao.getAllDailyNotes().size)
    }
}
