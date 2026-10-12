package com.gokcank.curalis.presentation.startup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.ReminderPermissions

/**
 * Hesap gerektirmeyen, iki ekranlık ilk kurulum karşılaması. Yalnızca uygulamanın ne işe
 * yaradığını kısaca anlatır ve hatırlatmalar için gereken izinleri nazikçe açıklar;
 * herhangi bir kişisel bilgi toplamaz, atlanabilir/geri dönülemez bir kayıt adımı yoktur.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val pageCount = 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when (page) {
                0 -> WelcomePage()
                else -> PermissionsPage()
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            PageDots(pageCount = pageCount, currentPage = page)

            Spacer(modifier = Modifier.height(20.dp))

            if (page < pageCount - 1) {
                Button(
                    onClick = { page++ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.onboarding_continue))
                }
            } else {
                Button(
                    onClick = onFinished,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.onboarding_start))
                }
            }
        }
    }
}

@Composable
private fun PageDots(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateFloatAsState(if (isActive) 24f else 8f, label = "dotWidth")
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = width.dp, height = 8.dp)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionsPage() {
    val context = LocalContext.current

    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var exactAlarmGranted by remember { mutableStateOf(ReminderPermissions.canScheduleExactAlarms(context)) }
    var fullScreenGranted by remember { mutableStateOf(ReminderPermissions.canUseFullScreenIntent(context)) }
    var batteryExempt by remember { mutableStateOf(ReminderPermissions.isIgnoringBatteryOptimizations(context)) }

    // Yalnızca sayfa açıldığında eksik olan izinlerin satırı gösterilir: Android 13+ tam zamanlı
    // alarm iznini kendiliğinden verir, yeni cihazlarda tam ekran izni de çoğu zaman açıktır.
    // Bildirim satırı her zaman gösterilir. Satırlar sonradan izin verilince kaybolmaz,
    // "Etkin" olarak kalır.
    val showExactAlarm = remember { !exactAlarmGranted }
    val showFullScreen = remember { !fullScreenGranted }
    val showBattery = remember { !batteryExempt }

    // Bu izinler sistem ayarlarında veriliyor; kullanıcı oradan dönünce durum yeniden okunur.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                exactAlarmGranted = ReminderPermissions.canScheduleExactAlarms(context)
                fullScreenGranted = ReminderPermissions.canUseFullScreenIntent(context)
                batteryExempt = ReminderPermissions.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> notificationsGranted = granted }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_permissions_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_permissions_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        PermissionRow(
            icon = Icons.Default.NotificationsActive,
            title = stringResource(R.string.onboarding_permission_notifications_title),
            description = stringResource(R.string.onboarding_permission_notifications_desc),
            isGranted = notificationsGranted,
            onRequestClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        if (showExactAlarm) {
            Spacer(modifier = Modifier.height(16.dp))
            PermissionRow(
                icon = Icons.Default.Alarm,
                title = stringResource(R.string.onboarding_permission_exact_alarm_title),
                description = stringResource(R.string.onboarding_permission_exact_alarm_desc),
                isGranted = exactAlarmGranted,
                onRequestClick = { ReminderPermissions.openExactAlarmSettings(context) }
            )
        }

        if (showFullScreen) {
            Spacer(modifier = Modifier.height(16.dp))
            PermissionRow(
                icon = Icons.Default.Fullscreen,
                title = stringResource(R.string.onboarding_permission_full_screen_title),
                description = stringResource(R.string.onboarding_permission_full_screen_desc),
                isGranted = fullScreenGranted,
                onRequestClick = { ReminderPermissions.openFullScreenIntentSettings(context) }
            )
        }

        if (showBattery) {
            Spacer(modifier = Modifier.height(16.dp))
            PermissionRow(
                icon = Icons.Default.BatteryChargingFull,
                title = stringResource(R.string.onboarding_permission_battery_title),
                description = stringResource(R.string.onboarding_permission_battery_desc),
                isGranted = batteryExempt,
                onRequestClick = { ReminderPermissions.requestIgnoreBatteryOptimizations(context) }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Text(
                stringResource(R.string.onboarding_permission_granted),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            OutlinedButton(
                onClick = onRequestClick,
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(stringResource(R.string.onboarding_permission_enable))
            }
        }
    }
}
