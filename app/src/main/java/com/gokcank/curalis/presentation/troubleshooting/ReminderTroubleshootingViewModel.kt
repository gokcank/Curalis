package com.gokcank.curalis.presentation.troubleshooting

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.notification.ManufacturerAutostartHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TroubleshootingUiState(
    val exactAlarmGranted: Boolean = true,
    val batteryOptimizationIgnored: Boolean = true,
    val manufacturer: ManufacturerAutostartHelper.KnownManufacturer = ManufacturerAutostartHelper.KnownManufacturer.OTHER
)

/**
 * Hatırlatıcı Sorun Giderme ekranının durumu. Her iki izin de dış Ayarlar ekranlarından
 * değiştirildiği için (uygulama içinden doğrudan değiştirilemez), ekran her ön plana
 * geldiğinde [refresh] çağrılarak yeniden okunur.
 */
@HiltViewModel
class ReminderTroubleshootingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TroubleshootingUiState(manufacturer = ManufacturerAutostartHelper.detect())
    )
    val uiState: StateFlow<TroubleshootingUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        _uiState.value = _uiState.value.copy(
            exactAlarmGranted = exactAlarmGranted,
            batteryOptimizationIgnored = batteryOptimizationIgnored
        )
    }
}
