package com.gokcank.curalis.presentation.notificationsettings

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.notification.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class NotificationSettingsUiState(
    val hideMedicationNameOnLockScreen: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = NotificationPreferences.DEFAULT_QUIET_START,
    val quietHoursEndMinutes: Int = NotificationPreferences.DEFAULT_QUIET_END
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferences: NotificationPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NotificationSettingsUiState(
            hideMedicationNameOnLockScreen = preferences.hideMedicationNameOnLockScreen,
            quietHoursEnabled = preferences.quietHoursEnabled,
            quietHoursStartMinutes = preferences.quietHoursStartMinutes,
            quietHoursEndMinutes = preferences.quietHoursEndMinutes
        )
    )
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    fun onHideLockScreenNameToggled(enabled: Boolean) {
        preferences.hideMedicationNameOnLockScreen = enabled
        _uiState.value = _uiState.value.copy(hideMedicationNameOnLockScreen = enabled)
    }

    fun onQuietHoursToggled(enabled: Boolean) {
        preferences.quietHoursEnabled = enabled
        _uiState.value = _uiState.value.copy(quietHoursEnabled = enabled)
    }

    fun onQuietHoursStartChanged(minutes: Int) {
        preferences.quietHoursStartMinutes = minutes
        _uiState.value = _uiState.value.copy(quietHoursStartMinutes = minutes)
    }

    fun onQuietHoursEndChanged(minutes: Int) {
        preferences.quietHoursEndMinutes = minutes
        _uiState.value = _uiState.value.copy(quietHoursEndMinutes = minutes)
    }
}
