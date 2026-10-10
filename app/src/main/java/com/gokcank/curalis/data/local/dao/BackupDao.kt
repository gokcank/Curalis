package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

@Dao
interface BackupDao {
    @Query("SELECT * FROM medications")
    suspend fun getAllMedications(): List<MedicationEntity>

    @Query("SELECT * FROM medication_days")
    suspend fun getAllMedicationDays(): List<MedicationDaysEntity>

    @Query("SELECT * FROM medication_times")
    suspend fun getAllMedicationTimes(): List<MedicationTimeEntity>

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctors(): List<DoctorEntity>

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointments(): List<AppointmentEntity>

    @Query("SELECT * FROM vitals")
    suspend fun getAllVitals(): List<VitalEntity>

    @Query("SELECT * FROM stock_history")
    suspend fun getAllStockHistory(): List<StockHistoryEntity>

    @Query("SELECT * FROM emergency_contacts")
    suspend fun getAllEmergencyContacts(): List<EmergencyContactEntity>

    @Query("SELECT * FROM symptoms")
    suspend fun getAllSymptoms(): List<SymptomEntity>

    @Query("SELECT * FROM vital_reminders")
    suspend fun getAllVitalReminders(): List<VitalReminderEntity>

    @Query("SELECT * FROM daily_notes")
    suspend fun getAllDailyNotes(): List<DailyNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationDays(days: List<MedicationDaysEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationTimes(times: List<MedicationTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: List<VitalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(entries: List<StockHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContacts(contacts: List<EmergencyContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<SymptomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalReminders(settings: List<VitalReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyNotes(notes: List<DailyNoteEntity>)

    // Child tables first so the deletes never leave dangling foreign keys mid-transaction.
    @Query("DELETE FROM medication_times")
    suspend fun deleteAllMedicationTimes()

    @Query("DELETE FROM medication_days")
    suspend fun deleteAllMedicationDays()

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("DELETE FROM appointments")
    suspend fun deleteAllAppointments()

    @Query("DELETE FROM medications")
    suspend fun deleteAllMedications()

    @Query("DELETE FROM doctors")
    suspend fun deleteAllDoctors()

    @Query("DELETE FROM vitals")
    suspend fun deleteAllVitals()

    @Query("DELETE FROM stock_history")
    suspend fun deleteAllStockHistory()

    @Query("DELETE FROM emergency_contacts")
    suspend fun deleteAllEmergencyContacts()

    @Query("DELETE FROM symptoms")
    suspend fun deleteAllSymptoms()

    @Query("DELETE FROM vital_reminders")
    suspend fun deleteAllVitalReminders()

    @Query("DELETE FROM daily_notes")
    suspend fun deleteAllDailyNotes()
}
