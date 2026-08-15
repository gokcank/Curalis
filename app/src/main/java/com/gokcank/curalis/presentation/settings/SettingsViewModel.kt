package com.gokcank.curalis.presentation.settings

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.security.AppLockPreferences
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.core.theme.ThemeMode
import com.gokcank.curalis.core.timeline.TimelinePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val timelinePreferences: TimelinePreferences
) : ViewModel() {

    val themeMode = themeController.themeMode

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
