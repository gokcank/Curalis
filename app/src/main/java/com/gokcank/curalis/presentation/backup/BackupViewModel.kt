package com.gokcank.curalis.presentation.backup

import android.content.Context
import android.net.Uri
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
import com.gokcank.curalis.data.backup.DriveBackupCodec
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

    /** Otomatik yedek bölümünün durumu (bkz. ADR-011, ADR-013). */
    val autoBackup: StateFlow<AutoBackupState> = _autoBackup.asStateFlow()

    private fun readAutoBackupState() = AutoBackupState(
        enabled = autoBackupPreferences.enabled,
        encryptEnabled = autoBackupPreferences.encryptEnabled,
        lastSuccessAt = autoBackupPreferences.lastSuccessAt,
        lastFailure = autoBackupPreferences.lastFailure?.let { name ->
            AutoBackupRunner.Failure.entries.find { it.name == name }
        },
        driveConnected = driveAccountPreferences.connectedEmail != null
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

    /** Otomatik yedeği açar ve ilk yedeği hemen alır; yalnızca Drive bağlıyken (ADR-013). */
    fun enableAutoBackup() {
        if (driveAccountPreferences.connectedEmail == null) return
        autoBackupPreferences.enabled = true
        AutoBackupWorker.scheduleIfEnabled(appContext, autoBackupPreferences)
        backupNow()
    }

    fun disableAutoBackup() {
        autoBackupPreferences.enabled = false
        autoBackupPreferences.lastFailure = null
        AutoBackupWorker.cancel(appContext)
        refreshAutoBackup()
    }

    /** "Yedeği şifrele"yi açar: şifre bir kez saklanır, sonraki Drive yedekleri onunla şifrelenir. */
    fun enableEncryption(password: String) {
        backupPasswordStore.save(password)
        autoBackupPreferences.encryptEnabled = true
        afterEncryptionChange()
    }

    /** Sonraki yedekler şifresiz alınır; saklanan şifre eski şifreli yedekleri açmak için kalır. */
    fun disableEncryption() {
        autoBackupPreferences.encryptEnabled = false
        afterEncryptionChange()
    }

    fun changeBackupPassword(password: String) {
        backupPasswordStore.save(password)
        afterEncryptionChange()
    }

    /** Şifreleme değiştiyse Drive'daki en yeni yedek hemen yeni biçimde alınır. */
    private fun afterEncryptionChange() {
        if (autoBackupPreferences.lastFailure == AutoBackupRunner.Failure.NO_PASSWORD.name) {
            autoBackupPreferences.lastFailure = null
        }
        if (autoBackupPreferences.enabled) backupNow() else refreshAutoBackup()
    }

    fun backupNow() {
        refreshAutoBackup(running = true)
        viewModelScope.launch {
            val outcome = try {
                autoBackupRunner.run(force = true)
            } catch (e: Exception) {
                AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.DRIVE_UNAVAILABLE)
            }
            refreshAutoBackup()
            _uiState.value = when (outcome) {
                is AutoBackupRunner.Outcome.Failed -> BackupUiState.Error(str(autoBackupFailureMessage(outcome.reason)))
                is AutoBackupRunner.Outcome.Done -> BackupUiState.Success(str(R.string.auto_backup_done, outcome.fileName))
                AutoBackupRunner.Outcome.Unchanged -> BackupUiState.Idle
            }
        }
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
        // Otomatik yedek yalnızca Drive'a gider; bağlantı kesilince kapanır (ADR-013).
        disableAutoBackup()
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

    /** Elle Drive yedeği: şifre sorulmaz; "Yedeği şifrele" açıksa saklanan şifreyle şifrelenir (ADR-013). */
    fun uploadToGoogleDrive(accessToken: String) {
        val password = if (autoBackupPreferences.encryptEnabled) {
            backupPasswordStore.load() ?: run {
                _uiState.value = BackupUiState.Error(str(R.string.auto_backup_error_password))
                return
            }
        } else {
            null
        }
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading(str(R.string.backup_uploading_to_drive))
            try {
                val content = DriveBackupCodec.encode(backupManager.exportData(), password)
                val driveManager = googleDriveManagerFactory.create(accessToken)
                // Her elle yedek ayrı, tarihli bir dosyadır; önceki yedeğin üzerine yazmaz.
                withContext(Dispatchers.IO) {
                    driveManager.createAppFile(DriveBackupNames.manualName(System.currentTimeMillis()), content)
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

    private val _drivePasswordRequest = MutableStateFlow<String?>(null)

    /** Şifreli bir Drive yedeği için şifre gerekiyorsa o yedeğin kimliği; pencere açıkken dolu. */
    val drivePasswordRequest: StateFlow<String?> = _drivePasswordRequest.asStateFlow()

    fun cancelDrivePasswordRequest() {
        _drivePasswordRequest.value = null
    }

    /**
     * Seçilen Drive yedeğini geri yükler. Şifresiz yedek doğrudan açılır; şifreli yedekte önce
     * telefonda saklanan şifre denenir, yoksa ya da açmıyorsa şifre sorulur ([typedPassword]).
     * Doğru şifre saklanır ve "Yedeği şifrele" açılır: kullanıcı şifreli yedeği seçmişti (ADR-013).
     */
    fun restoreDriveBackup(fileId: String, typedPassword: String? = null) {
        val token = driveRestoreToken ?: return
        _drivePasswordRequest.value = null
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
                val jsonString = when (val decoded = DriveBackupCodec.decode(fileContent, backupPasswordStore.load(), typedPassword)) {
                    is DriveBackupCodec.Decoded.Opened -> {
                        decoded.passwordToStore?.let { password ->
                            backupPasswordStore.save(password)
                            autoBackupPreferences.encryptEnabled = true
                        }
                        decoded.json
                    }
                    DriveBackupCodec.Decoded.NeedsPassword -> {
                        _uiState.value = BackupUiState.Idle
                        _drivePasswordRequest.value = fileId
                        return@launch
                    }
                    DriveBackupCodec.Decoded.WrongPassword -> {
                        // Yedeği yeniden seçtirmeden şifre yeniden sorulur.
                        _uiState.value = BackupUiState.Error(str(R.string.backup_wrong_password))
                        _drivePasswordRequest.value = fileId
                        return@launch
                    }
                }
                val recordCount = restoreAndRescheduleAlarms(jsonString)
                refreshAutoBackup()
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
    val encryptEnabled: Boolean = false,
    val lastSuccessAt: Long = 0L,
    val lastFailure: AutoBackupRunner.Failure? = null,
    val driveConnected: Boolean = false,
    val running: Boolean = false
)

fun autoBackupFailureMessage(reason: AutoBackupRunner.Failure): Int = when (reason) {
    AutoBackupRunner.Failure.NO_PASSWORD -> R.string.auto_backup_error_password
    AutoBackupRunner.Failure.NOT_CONFIGURED, AutoBackupRunner.Failure.DRIVE_NEEDS_PERMISSION -> R.string.auto_backup_error_drive_permission
    AutoBackupRunner.Failure.DRIVE_UNAVAILABLE -> R.string.auto_backup_error_drive_unavailable
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Loading(val message: String) : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
