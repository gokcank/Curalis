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
            vitals = backupDao.getAllVitals(),
            stockHistory = backupDao.getAllStockHistory(),
            emergencyContacts = backupDao.getAllEmergencyContacts(),
            symptoms = backupDao.getAllSymptoms(),
            vitalReminders = backupDao.getAllVitalReminders(),
            dailyNotes = backupDao.getAllDailyNotes()
        )

        return@withContext gson.toJson(backupData)
    }

    override suspend fun importData(jsonString: String): Int? = withContext(Dispatchers.IO) {
        try {
            val backupData = gson.fromJson(jsonString, BackupData::class.java)
            val backupDao = database.backupDao

            database.withTransaction {
                // The original seven types are present in every backup version, so they are
                // always replaced.
                backupDao.deleteAllMedicationTimes()
                backupDao.deleteAllMedicationDays()
                backupDao.deleteAllReminders()
                backupDao.deleteAllAppointments()
                backupDao.deleteAllMedications()
                backupDao.deleteAllDoctors()
                backupDao.deleteAllVitals()

                // Types added in backup version 2: a version-1 file has no such key (null),
                // and restoring it must not wipe data the backup never contained.
                // Emergency contacts go before appointments, which reference them.
                backupData.emergencyContacts?.let {
                    backupDao.deleteAllEmergencyContacts()
                    backupDao.insertEmergencyContacts(it)
                }

                backupDao.insertMedications(backupData.medications)
                backupDao.insertMedicationDays(backupData.medicationDays)
                backupDao.insertMedicationTimes(backupData.medicationTimes)
                backupDao.insertReminders(backupData.reminders)
                backupDao.insertDoctors(backupData.doctors)
                backupDao.insertVitals(backupData.vitals)

                // An appointment can point at a contact a version-1 backup never contained;
                // inserting that dangling reference would fail the whole restore on the
                // foreign key, so it is cleared instead (same as deleting the contact would).
                val doctorIds = backupData.doctors.mapTo(HashSet()) { it.id }
                val contactIds = backupDao.getAllEmergencyContacts().mapTo(HashSet()) { it.id }
                backupDao.insertAppointments(
                    backupData.appointments.map { appointment ->
                        appointment.copy(
                            doctorId = appointment.doctorId?.takeIf { it in doctorIds },
                            emergencyContactId = appointment.emergencyContactId?.takeIf { it in contactIds }
                        )
                    }
                )

                backupData.stockHistory?.let {
                    backupDao.deleteAllStockHistory()
                    backupDao.insertStockHistory(it)
                }
                backupData.symptoms?.let {
                    backupDao.deleteAllSymptoms()
                    backupDao.insertSymptoms(it)
                }
                backupData.vitalReminders?.let {
                    backupDao.deleteAllVitalReminders()
                    backupDao.insertVitalReminders(it)
                }
                backupData.dailyNotes?.let {
                    backupDao.deleteAllDailyNotes()
                    backupDao.insertDailyNotes(it)
                }
            }
            // Shown to the user so a restore of an empty or stale backup can be told apart
            // from a real one; settings-like and log-like rows are not counted.
            backupData.medications.size + backupData.doctors.size +
                backupData.appointments.size + backupData.vitals.size +
                backupData.emergencyContacts.orEmpty().size +
                backupData.symptoms.orEmpty().size +
                backupData.dailyNotes.orEmpty().size
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
