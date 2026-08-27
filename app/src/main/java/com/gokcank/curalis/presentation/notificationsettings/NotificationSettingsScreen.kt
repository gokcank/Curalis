package com.gokcank.curalis.presentation.notificationsettings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.gokcank.curalis.core.notification.NotificationPopupMode
import com.gokcank.curalis.core.notification.NotificationPreferences
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTroubleshooting: () -> Unit = {},
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showMorningTimePicker by remember { mutableStateOf(false) }
    var showWeekendTimePicker by remember { mutableStateOf(false) }

    fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format("%02d:%02d", h, m)
    }

    @Composable
    fun formatAppointmentLead(minutes: Int): String = when (minutes) {
        0 -> stringResource(R.string.off_label)
        in 1..59 -> stringResource(R.string.minutes_short_format, minutes)
        in 60..1439 -> stringResource(R.string.hours_short_format, minutes / 60)
        in 1440..10079 -> stringResource(R.string.days_short_format, minutes / 1440)
        else -> stringResource(R.string.weeks_short_format, minutes / (7 * 1440))
    }

    val themeChipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        containerColor = MaterialTheme.colorScheme.surface,
        labelColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_settings_title), color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.reminders_not_on_time_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(R.string.reminders_not_on_time_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Button(onClick = onNavigateToTroubleshooting) {
                        Text(stringResource(R.string.start_troubleshooting_button))
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.privacy_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.hide_med_name_lock_screen_title), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.hide_med_name_lock_screen_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.hideMedicationNameOnLockScreen,
                    onCheckedChange = viewModel::onHideLockScreenNameToggled
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.quiet_hours_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.quiet_hours_enable_title), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.quiet_hours_enable_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.quietHoursEnabled,
                    onCheckedChange = viewModel::onQuietHoursToggled
                )
            }

            if (uiState.quietHoursEnabled) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.quiet_hours_start_button, formatMinutes(uiState.quietHoursStartMinutes)))
                    }
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.quiet_hours_end_button, formatMinutes(uiState.quietHoursEndMinutes)))
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.fullscreen_popup_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.fullscreen_popup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationPopupMode.entries.forEach { mode ->
                    val modeLabel = when (mode) {
                        NotificationPopupMode.ALWAYS -> stringResource(R.string.notification_popup_mode_always)
                        NotificationPopupMode.NEVER -> stringResource(R.string.notification_popup_mode_never)
                        NotificationPopupMode.SCREEN_ON_ONLY -> stringResource(R.string.notification_popup_mode_screen_on_only)
                    }
                    FilterChip(
                        selected = uiState.popupMode == mode,
                        onClick = { viewModel.onPopupModeChanged(mode) },
                        label = { Text(modeLabel) },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Text(
                stringResource(R.string.fullscreen_popup_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.snooze_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.snooze_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationPreferences.SNOOZE_OPTIONS_MINUTES.forEach { minutes ->
                    FilterChip(
                        selected = uiState.snoozeMinutes == minutes,
                        onClick = { viewModel.onSnoozeMinutesChanged(minutes) },
                        label = { Text(stringResource(R.string.minutes_short_format, minutes)) },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.appointment_reminder_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.appointment_reminder_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationPreferences.APPOINTMENT_LEAD_OPTIONS_MINUTES.forEach { minutes ->
                    FilterChip(
                        selected = uiState.appointmentReminderMinutesBefore == minutes,
                        onClick = { viewModel.onAppointmentReminderLeadChanged(minutes) },
                        label = { Text(formatAppointmentLead(minutes)) },
                        colors = themeChipColors
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.morning_reminder_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.morning_reminder_toggle_title), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.morning_reminder_toggle_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.morningReminderEnabled,
                    onCheckedChange = viewModel::onMorningReminderToggled
                )
            }

            if (uiState.morningReminderEnabled) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedButton(
                    onClick = { showMorningTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.morning_reminder_time_button, formatMinutes(uiState.morningReminderMinutes)))
                }

                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.weekend_mode_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.weekend_mode_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.weekendModeEnabled,
                        onCheckedChange = viewModel::onWeekendModeToggled
                    )
                }

                if (uiState.weekendModeEnabled) {
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedButton(
                        onClick = { showWeekendTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.weekend_reminder_time_button, formatMinutes(uiState.weekendMorningReminderMinutes)))
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.notification_categories_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.notification_categories_desc),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.manage_notification_categories_button))
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        val initial = uiState.quietHoursStartMinutes
        val timePickerState = rememberTimePickerState(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.quiet_hours_start_picker_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showStartTimePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(onClick = {
                            viewModel.onQuietHoursStartChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showStartTimePicker = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }

    if (showEndTimePicker) {
        val initial = uiState.quietHoursEndMinutes
        val timePickerState = rememberTimePickerState(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.quiet_hours_end_picker_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showEndTimePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(onClick = {
                            viewModel.onQuietHoursEndChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showEndTimePicker = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }

    if (showMorningTimePicker) {
        val initial = uiState.morningReminderMinutes
        val timePickerState = rememberTimePickerState(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showMorningTimePicker = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.morning_reminder_picker_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showMorningTimePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(onClick = {
                            viewModel.onMorningReminderMinutesChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showMorningTimePicker = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }

    if (showWeekendTimePicker) {
        val initial = uiState.weekendMorningReminderMinutes
        val timePickerState = rememberTimePickerState(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showWeekendTimePicker = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.weekend_reminder_picker_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showWeekendTimePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(onClick = {
                            viewModel.onWeekendMorningReminderMinutesChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showWeekendTimePicker = false
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}
