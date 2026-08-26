package com.gokcank.curalis.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.security.AppLockPreferences
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.core.theme.ThemeMode
import com.gokcank.curalis.core.timeline.TimelinePreferences
import com.gokcank.curalis.core.utils.DatabaseExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class TimelineSlotBoundsUiState(
    val morningStartHour: Int = TimelinePreferences.DEFAULT_MORNING_START,
    val afternoonStartHour: Int = TimelinePreferences.DEFAULT_AFTERNOON_START,
    val eveningStartHour: Int = TimelinePreferences.DEFAULT_EVENING_START
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController,
    appLockPreferences: AppLockPreferences,
    private val timelinePreferences: TimelinePreferences,
    private val databaseExporter: DatabaseExporter
) : ViewModel() {

    val themeMode = themeController.themeMode

    private val _exportedDatabaseFile = MutableSharedFlow<File>()
    val exportedDatabaseFile = _exportedDatabaseFile.asSharedFlow()

    private val _exportError = MutableSharedFlow<String>()
    val exportError = _exportError.asSharedFlow()

    fun exportEncryptedDatabase() {
        viewModelScope.launch {
            try {
                val file = databaseExporter.exportEncryptedCopy()
                _exportedDatabaseFile.emit(file)
            } catch (e: Exception) {
                _exportError.emit("Veritabanı kopyası oluşturulamadı: ${e.localizedMessage ?: "Bilinmeyen hata"}")
            }
        }
    }

    fun shareExportedDatabase(context: android.content.Context, file: File) {
        databaseExporter.shareExportedCopy(context, file)
    }

    val isAppLockEnabled = appLockPreferences.lockEnabledState

    private val _timelineSlotBounds = MutableStateFlow(
        TimelineSlotBoundsUiState(
            morningStartHour = timelinePreferences.morningStartHour,
            afternoonStartHour = timelinePreferences.afternoonStartHour,
            eveningStartHour = timelinePreferences.eveningStartHour
        )
    )
    val timelineSlotBounds: StateFlow<TimelineSlotBoundsUiState> = _timelineSlotBounds.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = themeController.setThemeMode(mode)

    fun setMorningStartHour(hour: Int) {
        timelinePreferences.morningStartHour = hour
        _timelineSlotBounds.value = _timelineSlotBounds.value.copy(morningStartHour = hour)
    }

    fun setAfternoonStartHour(hour: Int) {
        timelinePreferences.afternoonStartHour = hour
        _timelineSlotBounds.value = _timelineSlotBounds.value.copy(afternoonStartHour = hour)
    }

    fun setEveningStartHour(hour: Int) {
        timelinePreferences.eveningStartHour = hour
        _timelineSlotBounds.value = _timelineSlotBounds.value.copy(eveningStartHour = hour)
    }
}
