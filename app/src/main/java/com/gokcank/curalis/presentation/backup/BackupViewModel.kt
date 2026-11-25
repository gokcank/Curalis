package com.gokcank.curalis.presentation.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.ReminderRescheduler
import com.gokcank.curalis.core.security.BackupCrypto
import com.gokcank.curalis.core.security.BackupPasswordStore
import com.gokcank.curalis.core.work.AutoBackupWorker
import com.gokcank.curalis.data.backup.AutoBackupPreferences
import com.gokcank.curalis.data.backup.AutoBackupRunner
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
import com.gokcank.curalis.data.backup.DriveBackupNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val backupManager: BackupManager,
    private val googleDriveManagerFactory: GoogleDriveManagerFactory,
    private val reminderRescheduler: ReminderRescheduler,
    private val driveAccountPreferences: DriveAccountPreferences,
    private val autoBackupPreferences: AutoBackupPreferences,
    private val backupPasswordStore: BackupPasswordStore,
    private val autoBackupRunner: AutoBackupRunner
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    /** Drive yedeği için bağlanılan hesap; bağlı değilse null. */
    private val _driveAccountEmail = MutableStateFlow(driveAccountPreferences.connectedEmail)
    val driveAccountEmail: StateFlow<String?> = _driveAccountEmail.asStateFlow()

    private val _autoBackup = MutableStateFlow(readAutoBackupState())

    /** Otomatik yedek bölümünün durumu (bkz. ADR-011). */
    val autoBackup: StateFlow<AutoBackupState> = _autoBackup.asStateFlow()

    private fun readAutoBackupState() = AutoBackupState(
        enabled = autoBackupPreferences.enabled,
        folderName = autoBackupPreferences.folderName,
        hasFolder = autoBackupPreferences.folderUri != null,
        lastSuccessAt = autoBackupPreferences.lastSuccessAt,
        lastFailure = autoBackupPreferences.lastFailure?.let { name ->
            AutoBackupRunner.Failure.entries.find { it.name == name }
        },
        driveConnected = driveAccountPreferences.connectedEmail != null,
        driveEnabled = autoBackupPreferences.driveEnabled,
        driveLastSuccessAt = autoBackupPreferences.driveLastSuccessAt,
        driveLastFailure = autoBackupPreferences.driveLastFailure?.let { name ->
            AutoBackupRunner.Failure.entries.find { it.name == name }
        }
    )

    /**
     * Günlük iş arka planda çalışır; ekran öne her geldiğinde (ör. hata bildirimine dokununca)
     * son durum yeniden okunur.
     */
    fun refreshAutoBackupState() {
        if (!_autoBackup.value.running) refreshAutoBackup()
    }

    private fun refreshAutoBackup(running: Boolean = false) {
        _autoBackup.value = readAutoBackupState().copy(running = running)
    }

    /**
     * Telefonun klasör seçicisinden dönen klasör. İzin kalıcı alınır ki günlük iş uygulama
     * kapalıyken de yazabilsin; önceki klasörün izni bırakılır.
     */
    fun onAutoBackupFolderChosen(uri: Uri) {
        val resolver = appContext.contentResolver
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        resolver.takePersistableUriPermission(uri, flags)
        autoBackupPreferences.folderUri?.takeIf { it != uri.toString() }?.let { previous ->
            runCatching { resolver.releasePersistableUriPermission(previous.toUri(), flags) }
        }
        autoBackupPreferences.folderUri = uri.toString()
        autoBackupPreferences.folderName = folderDisplayName(uri)
        refreshAutoBackup()
        // Klasör değiştiyse yeni klasöre hemen bir kopya alınır.
        if (autoBackupPreferences.enabled) backupNow()
    }

    private fun folderDisplayName(treeUri: Uri): String? = runCatching {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        appContext.contentResolver.query(
            documentUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null
        )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull()

    /** Klasör seçildikten ve parola girildikten sonra otomatik yedeği açar ve ilk yedeği alır. */
    fun enableAutoBackup(password: String) {
        backupPasswordStore.save(password)
        autoBackupPreferences.enabled = true
        AutoBackupWorker.scheduleIfEnabled(appContext, autoBackupPreferences)
        backupNow()
    }

    fun changeAutoBackupPassword(password: String) {
        backupPasswordStore.save(password)
        backupNow()
    }

    /** Kapatınca saklanan parola da silinir; klasör izni yeniden açmak için kalır. */
    fun disableAutoBackup() {
        autoBackupPreferences.enabled = false
        autoBackupPreferences.lastFailure = null
        backupPasswordStore.clear()
        AutoBackupWorker.cancel(appContext)
        refreshAutoBackup()
    }

    fun backupNow() {
        refreshAutoBackup(running = true)
        viewModelScope.launch {
            val report = try {
                autoBackupRunner.run(force = true)
            } catch (e: Exception) {
                AutoBackupRunner.Report(AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.FOLDER_UNAVAILABLE), null)
            }
            refreshAutoBackup()
            // Önce hata varsa o gösterilir; ikisi de başarılıysa klasördeki dosya adı.
            val failure = (report.local as? AutoBackupRunner.Outcome.Failed)?.reason
                ?: (report.drive as? AutoBackupRunner.Outcome.Failed)?.reason
            val done = report.local as? AutoBackupRunner.Outcome.Done
            _uiState.value = when {
                failure != null -> BackupUiState.Error(str(autoBackupFailureMessage(failure)))
                done != null -> BackupUiState.Success(str(R.string.auto_backup_done, done.fileName))
                else -> BackupUiState.Idle
            }
        }
    }

    /** Otomatik yedeğin Drive'a da gitmesi; açılınca hemen bir kopya gönderilir. */
    fun setDriveAutoBackup(enabled: Boolean) {
        autoBackupPreferences.driveEnabled = enabled
        if (!enabled) autoBackupPreferences.driveLastFailure = null
        refreshAutoBackup()
        if (enabled) backupNow()
    }

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
            refreshAutoBackup()
        }
    }

    fun disconnectDrive() {
        driveAccountPreferences.connectedEmail = null
        _driveAccountEmail.value = null
        // Bağlantı kesilince otomatik Drive yedeği de kapanır; yeniden bağlanınca ayrıca açılır.
        autoBackupPreferences.driveEnabled = false
        autoBackupPreferences.driveLastFailure = null
        refreshAutoBackup()
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
                // Her elle yedek ayrı, tarihli bir dosyadır; önceki yedeğin üzerine yazmaz.
                withContext(Dispatchers.IO) {
                    driveManager.createAppFile(DriveBackupNames.manualName(System.currentTimeMillis()), encrypted)
                }
                _uiState.value = BackupUiState.Success(str(R.string.backup_drive_upload_success))
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_drive_upload_failed))
            }
        }
    }

    private val _driveBackups = MutableStateFlow<List<DriveBackupNames.Info>?>(null)

    /** Geri yükleme için Drive'daki yedekler; seçim penceresi açıkken dolu. */
    val driveBackups: StateFlow<List<DriveBackupNames.Info>?> = _driveBackups.asStateFlow()

    /** Listeyi alırken verilen erişim anahtarı; seçilen yedek birkaç dakika içinde onunla indirilir. */
    private var driveRestoreToken: String? = null

    fun loadDriveBackups(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_downloading_from_drive))
            try {
                val files = withContext(Dispatchers.IO) { googleDriveManagerFactory.create(accessToken).listAppFiles() }
                val backups = DriveBackupNames.listForRestore(files)
                if (backups.isEmpty()) {
                    _uiState.value = BackupUiState.Error(str(R.string.backup_drive_no_backup_found))
                    return@launch
                }
                driveRestoreToken = accessToken
                _driveBackups.value = backups
                _uiState.value = BackupUiState.Idle
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_error_with_message, e.localizedMessage ?: ""))
            }
        }
    }

    fun dismissDriveBackups() {
        _driveBackups.value = null
    }

    fun restoreDriveBackup(fileId: String, password: String) {
        val token = driveRestoreToken ?: return
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_downloading_from_drive))
            try {
                val fileContent = withContext(Dispatchers.IO) {
                    googleDriveManagerFactory.create(token).downloadAppFile(fileId)
                }
                if (fileContent.isBlank()) {
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
                _uiState.value = if (recordCount != null) {
                    BackupUiState.Success(str(R.string.backup_drive_restore_success, recordCount))
                } else {
                    BackupUiState.Error(str(R.string.backup_restore_failed_file_corrupt))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error(str(R.string.backup_error_with_message, e.localizedMessage ?: ""))
            }
        }
    }
}

data class AutoBackupState(
    val enabled: Boolean = false,
    val folderName: String? = null,
    val hasFolder: Boolean = false,
    val lastSuccessAt: Long = 0L,
    val lastFailure: AutoBackupRunner.Failure? = null,
    val driveConnected: Boolean = false,
    val driveEnabled: Boolean = false,
    val driveLastSuccessAt: Long = 0L,
    val driveLastFailure: AutoBackupRunner.Failure? = null,
    val running: Boolean = false
)

fun autoBackupFailureMessage(reason: AutoBackupRunner.Failure): Int = when (reason) {
    AutoBackupRunner.Failure.NO_PASSWORD -> R.string.auto_backup_error_password
    AutoBackupRunner.Failure.FOLDER_UNAVAILABLE, AutoBackupRunner.Failure.NOT_CONFIGURED -> R.string.auto_backup_error_folder
    AutoBackupRunner.Failure.DRIVE_NEEDS_PERMISSION -> R.string.auto_backup_error_drive_permission
    AutoBackupRunner.Failure.DRIVE_UNAVAILABLE -> R.string.auto_backup_error_drive_unavailable
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Loading(val message: String) : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
