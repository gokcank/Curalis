package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.AppointmentEntity
import com.gokcank.curalis.data.local.entity.DoctorEntity
import com.gokcank.curalis.data.local.entity.MedicationDaysEntity
import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.data.local.entity.VitalEntity

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
}
