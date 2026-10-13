package com.gokcank.curalis.presentation.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.ReminderRescheduler
import com.gokcank.curalis.core.security.BackupCrypto
import com.gokcank.curalis.data.backup.DriveAccountPreferences
import com.gokcank.curalis.data.backup.GoogleDriveManagerFactory
import com.gokcank.curalis.domain.repository.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val backupManager: BackupManager,
    private val googleDriveManagerFactory: GoogleDriveManagerFactory,
    private val reminderRescheduler: ReminderRescheduler,
    private val driveAccountPreferences: DriveAccountPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    /** Drive yedeği için bağlanılan hesap; bağlı değilse null. */
    private val _driveAccountEmail = MutableStateFlow(driveAccountPreferences.connectedEmail)
    val driveAccountEmail: StateFlow<String?> = _driveAccountEmail.asStateFlow()

    /** Kullanıcı Drive iznini verdikten sonra, iznin hangi hesaba verildiğini öğrenip saklar. */
    fun connectDrive(accessToken: String) {
        viewModelScope.launch {
            val email = googleDriveManagerFactory.create(accessToken).fetchAccountEmail()
            if (email == null) {
                _uiState.value = BackupUiState.Error(str(R.string.drive_connect_failed))
                return@launch
            }
            driveAccountPreferences.connectedEmail = email
            _driveAccountEmail.value = email
        }
    }

    fun disconnectDrive() {
        driveAccountPreferences.connectedEmail = null
        _driveAccountEmail.value = null
    }

    private fun str(resId: Int, vararg args: Any): String = appContext.getString(resId, *args)

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }

    /**
     * Restores the backup and then swaps the alarms over to the restored data. The old
     * alarms are only cancelled once the import has succeeded — a failed import rolls back
     * and leaves the old data, whose alarms are still correct.
     */
    private suspend fun restoreAndRescheduleAlarms(jsonString: String): Int? {
        val previousAlarms = reminderRescheduler.snapshot()
        val recordCount = backupManager.importData(jsonString) ?: return null
        reminderRescheduler.cancel(previousAlarms)
        reminderRescheduler.scheduleAll()
        return recordCount
    }

    fun exportDataToUri(context: Context, uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_creating_file))
            try {
                val jsonString = backupManager.exportData()
                val encrypted = BackupCrypto.encrypt(jsonString, password)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(encrypted.toByteArray(Charsets.UTF_8))
                }

                _uiState.value = BackupUiState.Success(str(R.string.backup_saved_success))
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_error_generic, e.localizedMessage ?: ""))
            }
        }
    }

    fun importDataFromUri(context: Context, uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_restoring))
            try {
                val jsonBuilder = java.lang.StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            jsonBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }

                val fileContent = jsonBuilder.toString()
                if (fileContent.isBlank()) {
                    _uiState.value = BackupUiState.Error(str(R.string.backup_selected_file_empty))
                    return@launch
                }

                val jsonString = if (BackupCrypto.isEncrypted(fileContent)) {
                    try {
                        BackupCrypto.decrypt(fileContent, password)
                    } catch (e: Exception) {
                        _uiState.value = BackupUiState.Error(str(R.string.backup_wrong_password))
                        return@launch
                    }
                } else {
                    // Bu sürümden önce alınmış, şifrelenmemiş eski yedek dosyalarıyla uyumluluk.
                    fileContent
                }

                val recordCount = restoreAndRescheduleAlarms(jsonString)
                if (recordCount != null) {
                    _uiState.value = BackupUiState.Success(str(R.string.backup_restore_success, recordCount))
                } else {
                    _uiState.value = BackupUiState.Error(str(R.string.backup_restore_failed))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_restore_error_generic, e.localizedMessage ?: ""))
            }
        }
    }

    fun uploadToGoogleDrive(accessToken: String, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_uploading_to_drive))
            try {
                val jsonString = backupManager.exportData()
                val encrypted = BackupCrypto.encrypt(jsonString, password)
                val driveManager = googleDriveManagerFactory.create(accessToken)

                val success = driveManager.uploadBackupToDrive(encrypted, "curalis_drive_backup.json")
                if (success) {
                    _uiState.value = BackupUiState.Success(str(R.string.backup_drive_upload_success))
                } else {
                    _uiState.value = BackupUiState.Error(str(R.string.backup_drive_upload_failed))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_error_with_message, e.localizedMessage ?: ""))
            }
        }
    }

    fun downloadFromGoogleDrive(accessToken: String, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_downloading_from_drive))
            try {
                val driveManager = googleDriveManagerFactory.create(accessToken)
                val fileContent = driveManager.downloadBackupFromDrive("curalis_drive_backup.json")

                if (fileContent.isNullOrBlank()) {
                    _uiState.value = BackupUiState.Error(str(R.string.backup_drive_no_backup_found))
                    return@launch
                }

                val jsonString = if (BackupCrypto.isEncrypted(fileContent)) {
                    try {
                        BackupCrypto.decrypt(fileContent, password)
                    } catch (e: Exception) {
                        _uiState.value = BackupUiState.Error(str(R.string.backup_wrong_password))
                        return@launch
                    }
                } else {
                    fileContent
                }

                val recordCount = restoreAndRescheduleAlarms(jsonString)
                if (recordCount != null) {
                    _uiState.value = BackupUiState.Success(str(R.string.backup_drive_restore_success, recordCount))
                } else {
                    _uiState.value = BackupUiState.Error(str(R.string.backup_restore_failed_file_corrupt))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_error_with_message, e.localizedMessage ?: ""))
            }
        }
    }
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Loading(val message: String) : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
