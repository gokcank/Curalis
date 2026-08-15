package com.gokcank.curalis.presentation.notificationsettings

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.core.notification.NotificationPopupMode
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
    val quietHoursEndMinutes: Int = NotificationPreferences.DEFAULT_QUIET_END,
    val snoozeMinutes: Int = NotificationPreferences.DEFAULT_SNOOZE_MINUTES,
    val appointmentReminderMinutesBefore: Int = NotificationPreferences.DEFAULT_APPOINTMENT_LEAD_MINUTES,
    val popupMode: NotificationPopupMode = NotificationPopupMode.ALWAYS,
    val morningReminderEnabled: Boolean = false,
    val morningReminderMinutes: Int = NotificationPreferences.DEFAULT_MORNING_REMINDER_MINUTES,
    val weekendModeEnabled: Boolean = false,
    val weekendMorningReminderMinutes: Int = NotificationPreferences.DEFAULT_WEEKEND_MORNING_REMINDER_MINUTES
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferences: NotificationPreferences,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NotificationSettingsUiState(
            hideMedicationNameOnLockScreen = preferences.hideMedicationNameOnLockScreen,
            quietHoursEnabled = preferences.quietHoursEnabled,
            quietHoursStartMinutes = preferences.quietHoursStartMinutes,
            quietHoursEndMinutes = preferences.quietHoursEndMinutes,
            snoozeMinutes = preferences.snoozeMinutes,
            appointmentReminderMinutesBefore = preferences.appointmentReminderMinutesBefore,
            popupMode = preferences.popupMode,
            morningReminderEnabled = preferences.morningReminderEnabled,
            morningReminderMinutes = preferences.morningReminderMinutes,
            weekendModeEnabled = preferences.weekendModeEnabled,
            weekendMorningReminderMinutes = preferences.weekendMorningReminderMinutes
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

    fun onSnoozeMinutesChanged(minutes: Int) {
        preferences.snoozeMinutes = minutes
        _uiState.value = _uiState.value.copy(snoozeMinutes = minutes)
    }

    fun onAppointmentReminderLeadChanged(minutes: Int) {
        preferences.appointmentReminderMinutesBefore = minutes
        _uiState.value = _uiState.value.copy(appointmentReminderMinutesBefore = minutes)
    }

    fun onPopupModeChanged(mode: NotificationPopupMode) {
        preferences.popupMode = mode
        _uiState.value = _uiState.value.copy(popupMode = mode)
    }

    fun onMorningReminderToggled(enabled: Boolean) {
        preferences.morningReminderEnabled = enabled
        _uiState.value = _uiState.value.copy(morningReminderEnabled = enabled)
        alarmScheduler.scheduleMorningReminder()
    }

    fun onMorningReminderMinutesChanged(minutes: Int) {
        preferences.morningReminderMinutes = minutes
        _uiState.value = _uiState.value.copy(morningReminderMinutes = minutes)
        alarmScheduler.scheduleMorningReminder()
    }

    fun onWeekendModeToggled(enabled: Boolean) {
        preferences.weekendModeEnabled = enabled
        _uiState.value = _uiState.value.copy(weekendModeEnabled = enabled)
        alarmScheduler.scheduleMorningReminder()
    }

    fun onWeekendMorningReminderMinutesChanged(minutes: Int) {
        preferences.weekendMorningReminderMinutes = minutes
        _uiState.value = _uiState.value.copy(weekendMorningReminderMinutes = minutes)
        alarmScheduler.scheduleMorningReminder()
    }
}
