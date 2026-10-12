package com.gokcank.curalis.presentation.troubleshooting

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.notification.ManufacturerAutostartHelper
import com.gokcank.curalis.core.notification.ReminderPermissions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TroubleshootingUiState(
    val exactAlarmGranted: Boolean = true,
    val fullScreenIntentGranted: Boolean = true,
    val batteryOptimizationIgnored: Boolean = true,
    val manufacturer: ManufacturerAutostartHelper.KnownManufacturer = ManufacturerAutostartHelper.KnownManufacturer.OTHER
)

/**
 * Hatırlatıcı Sorun Giderme ekranının durumu. Bu izinlerin hepsi dış Ayarlar ekranlarından
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
        _uiState.value = _uiState.value.copy(
            exactAlarmGranted = ReminderPermissions.canScheduleExactAlarms(context),
            fullScreenIntentGranted = ReminderPermissions.canUseFullScreenIntent(context),
            batteryOptimizationIgnored = ReminderPermissions.isIgnoringBatteryOptimizations(context)
        )
    }
}
