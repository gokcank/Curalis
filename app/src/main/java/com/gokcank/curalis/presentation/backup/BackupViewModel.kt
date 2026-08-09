package com.gokcank.curalis.presentation.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.security.BackupCrypto
import com.gokcank.curalis.data.backup.GoogleDriveManagerFactory
import com.gokcank.curalis.domain.repository.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val googleDriveManagerFactory: GoogleDriveManagerFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }

    fun exportDataToUri(context: Context, uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Yedekleme dosyası oluşturuluyor...")
            try {
                val jsonString = backupManager.exportData()
                val encrypted = BackupCrypto.encrypt(jsonString, password)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(encrypted.toByteArray(Charsets.UTF_8))
                }

                _uiState.value = BackupUiState.Success("Yedekleme başarıyla kaydedildi.")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Yedekleme sırasında bir hata oluştu: ${e.localizedMessage}")
            }
        }
    }

    fun importDataFromUri(context: Context, uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Yedekleme geri yükleniyor...")
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
                    _uiState.value = BackupUiState.Error("Seçilen dosya boş.")
                    return@launch
                }

                val jsonString = if (BackupCrypto.isEncrypted(fileContent)) {
                    try {
                        BackupCrypto.decrypt(fileContent, password)
                    } catch (e: Exception) {
                        _uiState.value = BackupUiState.Error("Şifre yanlış veya yedek dosyası bozuk.")
                        return@launch
                    }
                } else {
                    // Bu sürümden önce alınmış, şifrelenmemiş eski yedek dosyalarıyla uyumluluk.
                    fileContent
                }

                val success = backupManager.importData(jsonString)
                if (success) {
                    _uiState.value = BackupUiState.Success("Veriler başarıyla geri yüklendi.")
                } else {
                    _uiState.value = BackupUiState.Error("Geri yükleme başarısız oldu. Yedek dosyası bozuk olabilir.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Geri yükleme sırasında bir hata oluştu: ${e.localizedMessage}")
            }
        }
    }

    fun uploadToGoogleDrive(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Google Drive'a yedekleniyor...")
            try {
                val jsonString = backupManager.exportData()
                val encrypted = BackupCrypto.encrypt(jsonString, password)
                val driveManager = googleDriveManagerFactory.create(account)

                val success = driveManager.uploadBackupToDrive(encrypted, "curalis_drive_backup.json")
                if (success) {
                    _uiState.value = BackupUiState.Success("Veriler Google Drive'a başarıyla yedeklendi.")
                } else {
                    _uiState.value = BackupUiState.Error("Google Drive yedeklemesi başarısız oldu.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Hata: ${e.localizedMessage}")
            }
        }
    }

    fun downloadFromGoogleDrive(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount, password: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Google Drive'dan geri yükleniyor...")
            try {
                val driveManager = googleDriveManagerFactory.create(account)
                val fileContent = driveManager.downloadBackupFromDrive("curalis_drive_backup.json")

                if (fileContent.isNullOrBlank()) {
                    _uiState.value = BackupUiState.Error("Google Drive'da yedek bulunamadı.")
                    return@launch
                }

                val jsonString = if (BackupCrypto.isEncrypted(fileContent)) {
                    try {
                        BackupCrypto.decrypt(fileContent, password)
                    } catch (e: Exception) {
                        _uiState.value = BackupUiState.Error("Şifre yanlış veya yedek dosyası bozuk.")
                        return@launch
                    }
                } else {
                    fileContent
                }

                val success = backupManager.importData(jsonString)
                if (success) {
                    _uiState.value = BackupUiState.Success("Google Drive yedeği başarıyla yüklendi.")
                } else {
                    _uiState.value = BackupUiState.Error("Geri yükleme başarısız oldu. Dosya bozuk olabilir.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Hata: ${e.localizedMessage}")
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
