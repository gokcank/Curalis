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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

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

    fun formatAppointmentLead(minutes: Int): String = when (minutes) {
        0 -> "Kapalı"
        in 1..59 -> "$minutes dk"
        in 60..1439 -> "${minutes / 60} saat"
        in 1440..10079 -> "${minutes / 1440} gün"
        else -> "${minutes / (7 * 1440)} hafta"
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
                title = { Text("Bildirim ve Hatırlatıcı Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
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
                        "Hatırlatıcılar zamanında gelmiyor mu?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Android sistem izinlerini ve telefon üreticinize özel ayarları kontrol edin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Button(onClick = onNavigateToTroubleshooting) {
                        Text("Sorun Gidermeyi Başlat")
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Gizlilik",
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
                    Text("Kilit ekranında ilaç adını gizle", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Açıkken, kilitli ekranda bildirim görünür ama ilacınızın adı gizlenir.",
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
                text = "Sessiz Saatler",
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
                    Text("Sessiz saatleri etkinleştir", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Bu saatler arasında hatırlatıcılar sessiz gelir ve ekranınızı uyandırmaz; bildirim yine de görünür kalır.",
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
                        Text("Başlangıç: ${formatMinutes(uiState.quietHoursStartMinutes)}")
                    }
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bitiş: ${formatMinutes(uiState.quietHoursEndMinutes)}")
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Tam Ekran Uyarı Popup'ı",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Bir doz hatırlatması geldiğinde, kilit ekranında da görünen tam ekran alarm uyarısı ne zaman gösterilsin?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationPopupMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.popupMode == mode,
                        onClick = { viewModel.onPopupModeChanged(mode) },
                        label = { Text(mode.displayNameTr) },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Text(
                "Doz \"kaçırıldı\" olarak işaretlenmeden önceki son hatırlatmada, bu ayardan bağımsız olarak popup her zaman gösterilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Erteleme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Bildirimdeki \"Ertele\" düğmesi dozu kaç dakika sonraya taşısın?",
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
                        label = { Text("$minutes dk") },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Randevu Hatırlatması",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Randevu hatırlatıcısı, randevudan ne kadar önce gelsin?",
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
                text = "Sabah Hatırlatması",
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
                    Text("Her sabah hatırlat", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "İlaçlarınızı yanınıza almanız için her sabah ayrı bir hatırlatma gönderilir.",
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
                    Text("Sabah saati: ${formatMinutes(uiState.morningReminderMinutes)}")
                }

                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hafta Sonu Modu", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Cumartesi ve Pazar günleri için ayrı bir sabah saati kullanın.",
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
                        Text("Hafta sonu saati: ${formatMinutes(uiState.weekendMorningReminderMinutes)}")
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Bildirim Kategorileri",
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
                        "Doz hatırlatmaları ve stok uyarıları artık ayrı bildirim kategorilerinde. " +
                            "Her birinin sesini, titreşimini veya görünürlüğünü ayrı ayrı kapatmak için sistem ayarlarını kullanabilirsiniz.",
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
                        Text("Bildirim Kategorilerini Sistem Ayarlarından Yönet")
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
                    Text("Sessiz Saatler Başlangıcı", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showStartTimePicker = false }) {
                            Text("İptal")
                        }
                        Button(onClick = {
                            viewModel.onQuietHoursStartChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showStartTimePicker = false
                        }) {
                            Text("Tamam")
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
                    Text("Sessiz Saatler Bitişi", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showEndTimePicker = false }) {
                            Text("İptal")
                        }
                        Button(onClick = {
                            viewModel.onQuietHoursEndChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showEndTimePicker = false
                        }) {
                            Text("Tamam")
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
                    Text("Sabah Hatırlatma Saati", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showMorningTimePicker = false }) {
                            Text("İptal")
                        }
                        Button(onClick = {
                            viewModel.onMorningReminderMinutesChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showMorningTimePicker = false
                        }) {
                            Text("Tamam")
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
                    Text("Hafta Sonu Sabah Saati", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showWeekendTimePicker = false }) {
                            Text("İptal")
                        }
                        Button(onClick = {
                            viewModel.onWeekendMorningReminderMinutesChanged(timePickerState.hour * 60 + timePickerState.minute)
                            showWeekendTimePicker = false
                        }) {
                            Text("Tamam")
                        }
                    }
                }
            }
        }
    }
}
