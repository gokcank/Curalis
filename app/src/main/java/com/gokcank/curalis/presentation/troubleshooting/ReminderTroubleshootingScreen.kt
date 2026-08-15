package com.gokcank.curalis.presentation.troubleshooting

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.ManufacturerAutostartHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTroubleshootingScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderTroubleshootingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Her iki izin de yalnızca dış Ayarlar ekranlarından değiştirilebiliyor; kullanıcı
    // oradan geri döndüğünde durumu yeniden okumamız gerekiyor.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hatırlatıcı Sorun Giderme") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Hatırlatıcılar zamanında gelmiyorsa, aşağıdaki ayarları kontrol edin. " +
                    "Bunlar Android işletim sisteminin ve telefon üreticinizin uyguladığı, " +
                    "uygulama dışından yönetilen kısıtlamalardır.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TroubleshootingItem(
                title = "Tam Zamanlı Alarm İzni",
                description = "Bu izin olmadan hatırlatıcılar birkaç dakika gecikmeyle gelebilir.",
                isOk = uiState.exactAlarmGranted,
                actionLabel = "Ayarları Aç",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    }
                }
            )

            TroubleshootingItem(
                title = "Pil Optimizasyonu",
                description = "Pil optimizasyonu açıkken Android, uygulamayı arka planda kapatıp alarmları engelleyebilir.",
                isOk = uiState.batteryOptimizationIgnored,
                actionLabel = "Muaf Tut",
                onAction = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    runCatching { context.startActivity(intent) }.onFailure {
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                }
            )

            val (title, body) = manufacturerInstructions(uiState.manufacturer)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(body, style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = {
                            val opened = ManufacturerAutostartHelper.tryOpenAutostartSettings(context)
                            if (!opened) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Text("Otomatik Başlatma Ayarını Açmayı Dene")
                    }
                }
            }
        }
    }
}

@Composable
private fun TroubleshootingItem(
    title: String,
    description: String,
    isOk: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isOk) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun manufacturerInstructions(manufacturer: ManufacturerAutostartHelper.KnownManufacturer): Pair<String, String> =
    when (manufacturer) {
        ManufacturerAutostartHelper.KnownManufacturer.XIAOMI -> "Xiaomi (MIUI) Cihazlar" to
            "Ayarlar > Uygulamalar > Uygulamaları Yönet > Curalis > Otomatik Başlat seçeneğini açın. " +
            "Ardından Güvenlik uygulaması > Uygulamalar > İzinler > Otomatik Başlatma listesinde de Curalis'i etkinleştirin."
        ManufacturerAutostartHelper.KnownManufacturer.HUAWEI -> "Huawei / Honor Cihazlar" to
            "Telefon Yöneticisi > Uygulama Başlatma bölümünden Curalis'i bulun, \"Elle Yönet\"i seçip " +
            "Otomatik Başlatma, İkincil Başlatma ve Arka Planda Çalışma seçeneklerinin hepsini açın."
        ManufacturerAutostartHelper.KnownManufacturer.SAMSUNG -> "Samsung Cihazlar" to
            "Ayarlar > Pil > Arka Plan Kullanım Sınırları'na girin. Curalis \"Uyuyan uygulamalar\" veya " +
            "\"Asla uyumayan uygulamalar dışında\" listesindeyse çıkarın; \"Kısıtlanmamış\" olarak işaretleyin."
        ManufacturerAutostartHelper.KnownManufacturer.OPPO -> "Oppo (ColorOS) Cihazlar" to
            "Ayarlar > Pil > Uygulama Pil Yönetimi'nden Curalis'i bulun ve \"İzin Ver\"i seçin; ayrıca " +
            "Güvenlik Merkezi > İzinler > Otomatik Başlatma listesinde de etkinleştirin."
        ManufacturerAutostartHelper.KnownManufacturer.VIVO -> "Vivo (FuntouchOS) Cihazlar" to
            "i Yöneticisi > Uygulama Yönetimi > Otomatik Başlatma Yönetimi'nden Curalis'i etkinleştirin."
        ManufacturerAutostartHelper.KnownManufacturer.ONEPLUS -> "OnePlus (OxygenOS) Cihazlar" to
            "Ayarlar > Pil > Pil Optimizasyonu'ndan Curalis'i \"Optimize Edilmedi\" yapın; ayrıca " +
            "Ayarlar > Uygulamalar > Otomatik Başlatma listesinde de etkinleştirin."
        ManufacturerAutostartHelper.KnownManufacturer.OTHER -> "Otomatik Başlatma" to
            "Bazı telefon üreticileri, pil tasarrufu için arka planda çalışan uygulamaları kısıtlar. " +
            "Ayarlar > Uygulamalar > Curalis > Pil bölümünden \"Kısıtlanmamış\"/\"Optimize Edilmedi\" seçin; " +
            "cihazınıza özel bir \"otomatik başlatma\" ayarı varsa onu da açın."
    }
