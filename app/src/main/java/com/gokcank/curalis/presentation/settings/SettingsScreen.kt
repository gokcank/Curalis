package com.gokcank.curalis.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.gokcank.curalis.core.timeline.TimelinePreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToAppLockSettings: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        val themeMode by viewModel.themeMode.collectAsState()
        val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
        val timelineSlotBounds by viewModel.timelineSlotBounds.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.appearance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.theme_mode), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.theme_mode_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.padding(vertical = 4.dp))

                val themeChipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        label = { Text(stringResource(R.string.theme_light)) },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        label = { Text(stringResource(R.string.theme_dark)) },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.AMOLED,
                        onClick = { viewModel.setThemeMode(ThemeMode.AMOLED) },
                        label = { Text(stringResource(R.string.theme_amoled)) },
                        colors = themeChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            // Language Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.language_desc), 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                var expanded by remember { mutableStateOf(false) }
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                val isEnglish = currentLocales.toLanguageTags().contains("en")
                val currentLangName = if (isEnglish) "English" else "Türkçe"

                Box {
                    Button(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(currentLangName)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Türkçe") },
                            onClick = {
                                expanded = false
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = {
                                expanded = false
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Bildirimler",
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
                    Text("Bildirim ve Hatırlatıcı Ayarları", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Sessiz saatler, kilit ekranı gizliliği ve bildirim kategorileri",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToNotificationSettings,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Yönet")
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Zaman Çizelgesi Dilimleri",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Günlük Zaman Çizelgesi ekranındaki Sabah/Öğle/Akşam dilimlerinin başlangıç saatlerini değiştirin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val slotChipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text("Sabah başlangıcı", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimelinePreferences.MORNING_START_OPTIONS.forEach { hour ->
                    FilterChip(
                        selected = timelineSlotBounds.morningStartHour == hour,
                        onClick = { viewModel.setMorningStartHour(hour) },
                        label = { Text(String.format("%02d:00", hour)) },
                        colors = slotChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text("Öğle başlangıcı", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimelinePreferences.AFTERNOON_START_OPTIONS.forEach { hour ->
                    FilterChip(
                        selected = timelineSlotBounds.afternoonStartHour == hour,
                        onClick = { viewModel.setAfternoonStartHour(hour) },
                        label = { Text(String.format("%02d:00", hour)) },
                        colors = slotChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text("Akşam başlangıcı", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimelinePreferences.EVENING_START_OPTIONS.forEach { hour ->
                    FilterChip(
                        selected = timelineSlotBounds.eveningStartHour == hour,
                        onClick = { viewModel.setEveningStartHour(hour) },
                        label = { Text(String.format("%02d:00", hour)) },
                        colors = slotChipColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.security),
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
                    Text(stringResource(R.string.app_lock), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(
                            if (isAppLockEnabled) R.string.app_lock_status_on
                            else R.string.app_lock_status_off
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToAppLockSettings,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(R.string.app_lock_manage))
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Veri Yönetimi",
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
                    Text("Yedekleme & Geri Yükleme", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Verilerinizi telefonunuza veya buluta yedekleyin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToBackup,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Yönet")
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "İpucu: Telefon değişikliğinde verilerinizi kaybetmemek için Google Drive'a şifreli bir yedek almanızı öneririz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Destek",
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
                    Text("Yardım Merkezi / SSS", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Sık sorulan sorular ve çözümleri",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToHelpCenter,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Görüntüle")
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Uygulamayı Paylaş", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Curalis'i arkadaşlarınızla ve ailenizle paylaşın",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "İlaçlarımı, randevularımı ve sağlık ölçümlerimi Curalis ile takip ediyorum — verileriniz yalnızca cihazınızda saklanır: https://play.google.com/store/apps/details?id=${context.packageName}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Curalis'i Paylaş"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paylaş")
                }
            }
        }
    }
}
