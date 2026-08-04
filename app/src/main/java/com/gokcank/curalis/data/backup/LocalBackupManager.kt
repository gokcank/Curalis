package com.gokcank.curalis.data.backup

import androidx.room.withTransaction
import com.gokcank.curalis.data.local.CuralisDatabase
import com.gokcank.curalis.domain.model.BackupData
import com.gokcank.curalis.domain.repository.BackupManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalBackupManager @Inject constructor(
    private val database: CuralisDatabase,
    private val gson: Gson
) : BackupManager {

    override suspend fun exportData(): String = withContext(Dispatchers.IO) {
        val backupDao = database.backupDao

        val backupData = BackupData(
            medications = backupDao.getAllMedications(),
            medicationDays = backupDao.getAllMedicationDays(),
            medicationTimes = backupDao.getAllMedicationTimes(),
            reminders = backupDao.getAllReminders(),
            doctors = backupDao.getAllDoctors(),
            appointments = backupDao.getAllAppointments(),
            vitals = backupDao.getAllVitals()
        )

        return@withContext gson.toJson(backupData)
    }

    override suspend fun importData(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupData = gson.fromJson(jsonString, BackupData::class.java)

            database.withTransaction {
                // Clear existing data
                database.clearAllTables()

                // Insert imported data
                val backupDao = database.backupDao
                backupDao.insertMedications(backupData.medications)
                backupDao.insertMedicationDays(backupData.medicationDays)
                backupDao.insertMedicationTimes(backupData.medicationTimes)
                backupDao.insertReminders(backupData.reminders)
                backupDao.insertDoctors(backupData.doctors)
                backupDao.insertAppointments(backupData.appointments)
                backupDao.insertVitals(backupData.vitals)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
