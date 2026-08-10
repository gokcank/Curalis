package com.gokcank.curalis.presentation.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.core.notification.NotificationHelper
import com.gokcank.curalis.core.notification.ReminderActionReceiver
import com.gokcank.curalis.core.theme.CuralisTheme
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.domain.model.SkipReason
import com.gokcank.curalis.presentation.components.SkipReasonDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmFullScreenActivity : ComponentActivity() {

    @Inject
    lateinit var themeController: ThemeController

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        turnScreenOnAndUnlock()
        startAlarmSound()

        val reminderId = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: ""
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: "İlaç Vakti"
        val medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: ""
        val dose = intent.getStringExtra(AlarmScheduler.EXTRA_DOSE)

        setContent {
            val themeMode by themeController.themeMode.collectAsState()

            CuralisTheme(themeMode = themeMode) {
                var showSkipReasonDialog by remember { mutableStateOf(false) }

                AlarmFullScreenContent(
                    medicationName = medicationName,
                    dose = dose,
                    onTakeClick = {
                        sendAction(NotificationHelper.ACTION_TAKEN, reminderId, medicationId, medicationName)
                        finish()
                    },
                    onSkipClick = { showSkipReasonDialog = true },
                    onSnoozeClick = { minutes ->
                        sendAction(NotificationHelper.ACTION_SNOOZE, reminderId, medicationId, medicationName, minutes)
                        finish()
                    }
                )

                if (showSkipReasonDialog) {
                    SkipReasonDialog(
                        onDismiss = { showSkipReasonDialog = false },
                        onConfirm = { reason ->
                            sendAction(NotificationHelper.ACTION_SKIP, reminderId, medicationId, medicationName, skipReason = reason)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun sendAction(
        action: String,
        reminderId: String,
        medicationId: String,
        medicationName: String,
        snoozeMinutes: Int = 10,
        skipReason: SkipReason? = null
    ) {
        stopAlarmSound()
        val intent = Intent(this, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
            putExtra(NotificationHelper.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationHelper.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            skipReason?.let { putExtra(NotificationHelper.EXTRA_SKIP_REASON, it.name) }
        }
        sendBroadcast(intent)
    }

    private fun turnScreenOnAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun startAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()

            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmSound() {
        try {
            ringtone?.stop()
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }
}

@Composable
fun AlarmFullScreenContent(
    medicationName: String,
    dose: String?,
    onTakeClick: () -> Unit,
    onSkipClick: () -> Unit,
    onSnoozeClick: (minutes: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "İlaç vakti geldi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = medicationName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                dose?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Doz: $it",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ALINDI
                Button(
                    onClick = onTakeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("İlacı aldım", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ATLANDI
                OutlinedButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bu dozu atla")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SNOOZE (ERTELE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSnoozeClick(10) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("10 dk", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { onSnoozeClick(30) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("30 dk", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
