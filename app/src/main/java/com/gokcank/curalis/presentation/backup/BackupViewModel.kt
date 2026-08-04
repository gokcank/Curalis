package com.gokcank.curalis.presentation.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }

    fun exportDataToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Yedekleme dosyası oluşturuluyor...")
            try {
                val jsonString = backupManager.exportData()
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                
                _uiState.value = BackupUiState.Success("Yedekleme başarıyla kaydedildi.")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Yedekleme sırasında bir hata oluştu: ${e.localizedMessage}")
            }
        }
    }

    fun importDataFromUri(context: Context, uri: Uri) {
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
                
                val jsonString = jsonBuilder.toString()
                if (jsonString.isNotBlank()) {
                    val success = backupManager.importData(jsonString)
                    if (success) {
                        _uiState.value = BackupUiState.Success("Veriler başarıyla geri yüklendi.")
                    } else {
                        _uiState.value = BackupUiState.Error("Geri yükleme başarısız oldu. Yedek dosyası bozuk olabilir.")
                    }
                } else {
                    _uiState.value = BackupUiState.Error("Seçilen dosya boş.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Geri yükleme sırasında bir hata oluştu: ${e.localizedMessage}")
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
