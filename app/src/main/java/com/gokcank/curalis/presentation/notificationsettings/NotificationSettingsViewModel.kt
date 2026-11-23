package com.gokcank.curalis.presentation.notificationsettings

import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.core.notification.LockScreenPrivacy
import com.gokcank.curalis.core.notification.NotificationPopupMode
import com.gokcank.curalis.core.notification.NotificationHelper
import com.gokcank.curalis.core.notification.NotificationPreferences
import com.gokcank.curalis.core.notification.ReminderRetryPolicy
import com.gokcank.curalis.core.notification.VoiceAvailability
import com.gokcank.curalis.core.notification.VoiceReminderPolicy
import com.gokcank.curalis.core.notification.VoiceReminderSpeaker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val lockScreenPrivacy: LockScreenPrivacy = LockScreenPrivacy.FULL,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = NotificationPreferences.DEFAULT_QUIET_START,
    val quietHoursEndMinutes: Int = NotificationPreferences.DEFAULT_QUIET_END,
    val snoozeMinutes: Int = NotificationPreferences.DEFAULT_SNOOZE_MINUTES,
    /** Seçili hatırlatma sesi; null = telefonun varsayılan bildirim sesi. */
    val reminderSoundUri: String? = null,
    val reminderSoundTitle: String? = null,
    val voiceReminderEnabled: Boolean = false,
    val voiceReminderSpeakName: Boolean = false,
    /** null: henüz denetlenmedi. */
    val voiceAvailability: VoiceAvailability? = null,
    val reminderAttempts: Int = ReminderRetryPolicy.DEFAULT_ATTEMPTS,
    val reminderRetryIntervalMinutes: Int = ReminderRetryPolicy.DEFAULT_INTERVAL_MINUTES,
    val lateDoseThresholdMinutes: Int = NotificationPreferences.DEFAULT_LATE_DOSE_THRESHOLD_MINUTES,
    val appointmentReminderMinutesBefore: Int = NotificationPreferences.DEFAULT_APPOINTMENT_LEAD_MINUTES,
    val popupMode: NotificationPopupMode = NotificationPopupMode.ALWAYS,
    val morningReminderEnabled: Boolean = false,
    val morningReminderMinutes: Int = NotificationPreferences.DEFAULT_MORNING_REMINDER_MINUTES,
    val weekendModeEnabled: Boolean = false,
    val weekendMorningReminderMinutes: Int = NotificationPreferences.DEFAULT_WEEKEND_MORNING_REMINDER_MINUTES,
    val eveningSummaryEnabled: Boolean = false,
    val eveningSummaryMinutes: Int = NotificationPreferences.DEFAULT_EVENING_SUMMARY_MINUTES
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferences: NotificationPreferences,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper,
    private val voiceReminderSpeaker: VoiceReminderSpeaker
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NotificationSettingsUiState(
            lockScreenPrivacy = preferences.lockScreenPrivacy,
            quietHoursEnabled = preferences.quietHoursEnabled,
            quietHoursStartMinutes = preferences.quietHoursStartMinutes,
            quietHoursEndMinutes = preferences.quietHoursEndMinutes,
            snoozeMinutes = preferences.snoozeMinutes,
            reminderSoundUri = preferences.reminderSoundUri,
            reminderSoundTitle = notificationHelper.reminderSoundTitle(),
            voiceReminderEnabled = preferences.voiceReminderEnabled,
            voiceReminderSpeakName = preferences.voiceReminderSpeakName,
            reminderAttempts = preferences.reminderAttempts,
            reminderRetryIntervalMinutes = preferences.reminderRetryIntervalMinutes,
            lateDoseThresholdMinutes = preferences.lateDoseThresholdMinutes,
            appointmentReminderMinutesBefore = preferences.appointmentReminderMinutesBefore,
            popupMode = preferences.popupMode,
            morningReminderEnabled = preferences.morningReminderEnabled,
            morningReminderMinutes = preferences.morningReminderMinutes,
            weekendModeEnabled = preferences.weekendModeEnabled,
            weekendMorningReminderMinutes = preferences.weekendMorningReminderMinutes,
            eveningSummaryEnabled = preferences.eveningSummaryEnabled,
            eveningSummaryMinutes = preferences.eveningSummaryMinutes
        )
    )
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    fun onLockScreenPrivacyChanged(privacy: LockScreenPrivacy) {
        preferences.lockScreenPrivacy = privacy
        _uiState.value = _uiState.value.copy(lockScreenPrivacy = privacy)
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

    /**
     * Ses seçicinin sonucu. Seçicideki "Varsayılan" seçeneği telefonun varsayılan bildirim sesini
     * gösteren genel adresi döndürür; o da varsayılan (null) olarak saklanır. Aynı ses yeniden
     * seçildiyse kanal boşuna yenilenmez.
     */
    fun onReminderSoundPicked(uri: Uri?) {
        val normalized = uri?.takeIf { it != Settings.System.DEFAULT_NOTIFICATION_URI }?.toString()
        if (normalized == preferences.reminderSoundUri) return
        notificationHelper.changeReminderSound(normalized?.let(Uri::parse))
        _uiState.value = _uiState.value.copy(
            reminderSoundUri = preferences.reminderSoundUri,
            reminderSoundTitle = notificationHelper.reminderSoundTitle()
        )
    }

    fun onVoiceReminderToggled(enabled: Boolean) {
        preferences.voiceReminderEnabled = enabled
        _uiState.value = _uiState.value.copy(voiceReminderEnabled = enabled)
        if (enabled) refreshVoiceAvailability()
    }

    fun onVoiceReminderSpeakNameToggled(enabled: Boolean) {
        preferences.voiceReminderSpeakName = enabled
        _uiState.value = _uiState.value.copy(voiceReminderSpeakName = enabled)
    }

    /** Ekran her açıldığında (ör. kullanıcı ses ayarlarından dil indirip döndüğünde) yeniden bakılır. */
    fun refreshVoiceAvailability() {
        if (!preferences.voiceReminderEnabled) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(voiceAvailability = voiceReminderSpeaker.checkAvailability())
        }
    }

    /** Örnek: kilit ekranı gizliliği izin veriyorsa ve istenmişse örnek bir ilaç adıyla. */
    fun onVoiceReminderSample(sampleMedicationName: String) {
        val withName = VoiceReminderPolicy.includesName(preferences.voiceReminderSpeakName, preferences.lockScreenPrivacy)
        voiceReminderSpeaker.speakSample(if (withName) sampleMedicationName else null)
    }

    fun onReminderAttemptsChanged(attempts: Int) {
        preferences.reminderAttempts = attempts
        _uiState.value = _uiState.value.copy(reminderAttempts = preferences.reminderAttempts)
    }

    fun onReminderRetryIntervalChanged(minutes: Int) {
        preferences.reminderRetryIntervalMinutes = minutes
        _uiState.value = _uiState.value.copy(reminderRetryIntervalMinutes = preferences.reminderRetryIntervalMinutes)
    }

    fun onLateDoseThresholdChanged(minutes: Int) {
        preferences.lateDoseThresholdMinutes = minutes
        _uiState.value = _uiState.value.copy(lateDoseThresholdMinutes = minutes)
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

    fun onEveningSummaryToggled(enabled: Boolean) {
        preferences.eveningSummaryEnabled = enabled
        _uiState.value = _uiState.value.copy(eveningSummaryEnabled = enabled)
        alarmScheduler.scheduleEveningSummary()
    }

    fun onEveningSummaryMinutesChanged(minutes: Int) {
        preferences.eveningSummaryMinutes = minutes
        _uiState.value = _uiState.value.copy(eveningSummaryMinutes = minutes)
        alarmScheduler.scheduleEveningSummary()
    }
}
