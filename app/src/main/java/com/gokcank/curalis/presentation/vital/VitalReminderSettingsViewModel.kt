package com.gokcank.curalis.presentation.vital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.model.VitalReminderSetting
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.repository.VitalReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VitalReminderSettingsViewModel @Inject constructor(
    private val vitalReminderRepository: VitalReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val settings: StateFlow<Map<VitalType, VitalReminderSetting>> = vitalReminderRepository.getAllSettings()
        .map { list -> list.associateBy { it.type } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun settingFor(type: VitalType): VitalReminderSetting {
        return settings.value[type] ?: VitalReminderSetting(type = type)
    }

    fun onToggle(type: VitalType, enabled: Boolean) {
        save(settingFor(type).copy(enabled = enabled))
    }

    fun onTimeChange(type: VitalType, hour: Int, minute: Int) {
        save(settingFor(type).copy(hour = hour, minute = minute))
    }

    fun onDayToggle(type: VitalType, day: Int) {
        val current = settingFor(type)
        val newDays = if (current.daysOfWeek.contains(day)) {
            current.daysOfWeek - day
        } else {
            current.daysOfWeek + day
        }
        save(current.copy(daysOfWeek = newDays))
    }

    private fun save(setting: VitalReminderSetting) {
        viewModelScope.launch {
            vitalReminderRepository.saveSetting(setting)
            if (setting.enabled) {
                alarmScheduler.scheduleVitalReminder(setting.type, setting.hour, setting.minute, setting.daysOfWeek)
            } else {
                alarmScheduler.cancelVitalReminder(setting.type)
            }
        }
    }
}
