package com.gokcank.curalis.presentation.alarm

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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Medication
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.core.notification.NotificationHelper
import com.gokcank.curalis.core.notification.ReminderActionReceiver
import com.gokcank.curalis.core.theme.CuralisTheme
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.domain.model.SkipReason
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.presentation.components.SkipReasonDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AlarmFullScreenActivity : ComponentActivity() {

    @Inject
    lateinit var themeController: ThemeController

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    private val viewModel: AlarmFullScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        showOverLockScreen()
        startAlarmSound()

        // Liste veritabanından gelene kadar (ya da doz kaydı henüz yazılmamışsa) ekranı açan
        // dozu göstermek için yedek satır.
        val launchingDose = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID)?.let { reminderId ->
            AlarmDose(
                reminder = Reminder(
                    id = reminderId,
                    medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: "",
                    timeInMillis = System.currentTimeMillis()
                ),
                medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME)
                    ?: getString(R.string.alarm_default_medication_name),
                dosage = intent.getStringExtra(AlarmScheduler.EXTRA_DOSE)
            )
        }

        setContent {
            val themeMode by themeController.themeMode.collectAsState()
            val loadedDoses by viewModel.doses.collectAsState()
            var userActed by remember { mutableStateOf(false) }
            var skipTarget by remember { mutableStateOf<AlarmDose?>(null) }

            // Kullanıcı yanıt verdikçe dozlar listeden düşer; hepsi yanıtlanınca ekran kapanır.
            // Açılış anında liste boşsa (doz kaydı henüz yazılmamış olabilir) ekranı açan doz gösterilir.
            val doses = loadedDoses?.takeIf { it.isNotEmpty() || userActed } ?: listOfNotNull(launchingDose)
            LaunchedEffect(userActed, loadedDoses) {
                if (userActed && loadedDoses?.isEmpty() == true) finish()
            }

            CuralisTheme(themeMode = themeMode) {
                AlarmFullScreenContent(
                    doses = doses,
                    onTakeClick = { dose ->
                        userActed = true
                        sendAction(NotificationHelper.ACTION_TAKEN, dose)
                        if (doses.size <= 1) finish()
                    },
                    onSkipClick = { dose -> skipTarget = dose },
                    onSnoozeClick = { dose, minutes ->
                        userActed = true
                        sendAction(NotificationHelper.ACTION_SNOOZE, dose, snoozeMinutes = minutes)
                        if (doses.size <= 1) finish()
                    },
                    onTakeAllClick = {
                        userActed = true
                        doses.forEach { sendAction(NotificationHelper.ACTION_TAKEN, it) }
                        finish()
                    }
                )

                skipTarget?.let { dose ->
                    SkipReasonDialog(
                        onDismiss = { skipTarget = null },
                        onConfirm = { reason ->
                            userActed = true
                            sendAction(NotificationHelper.ACTION_SKIP, dose, skipReason = reason)
                            skipTarget = null
                            if (doses.size <= 1) finish()
                        }
                    )
                }
            }
        }
    }

    /**
     * Ekran açıkken başka bir dozun hatırlatması gelirse ekran yeniden kurulmaz (önceden yeni
     * gelen eskisinin yerine geçiyordu); liste veritabanından kendiliğinden güncellenir. Kullanıcı
     * sesi bir yanıtla susturduysa yeni doz için yeniden çalınır.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (ringtone?.isPlaying != true) startAlarmSound()
    }

    private fun sendAction(
        action: String,
        dose: AlarmDose,
        snoozeMinutes: Int = 10,
        skipReason: SkipReason? = null
    ) {
        stopAlarmSound()
        val intent = Intent(this, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, dose.reminder.id)
            putExtra(NotificationHelper.EXTRA_MEDICATION_ID, dose.reminder.medicationId)
            putExtra(NotificationHelper.EXTRA_MEDICATION_NAME, dose.medicationName)
            putExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            skipReason?.let { putExtra(NotificationHelper.EXTRA_SKIP_REASON, it.name) }
        }
        sendBroadcast(intent)
    }

    /**
     * Hatırlatmayı kilit ekranının üstünde gösterir ve ekranı açar; kilidi açmaz. Aldım/Ertele/
     * Atla yayın (broadcast) olarak işlendiği için kilit açmayı gerektirmiyor. Önceden burada
     * kilidin açılması da isteniyordu: standart Android bunu sessizce yok sayarken Samsung her
     * hatırlatmada parmak izi/yüz doğrulama ekranını açıyordu.
     */
    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
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
    doses: List<AlarmDose>,
    onTakeClick: (AlarmDose) -> Unit,
    onSkipClick: (AlarmDose) -> Unit,
    onSnoozeClick: (AlarmDose, minutes: Int) -> Unit,
    onTakeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.alarm_medication_time_label),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (doses.size > 1) {
            Text(
                text = pluralStringResource(R.plurals.alarm_doses_count, doses.size, doses.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            items(doses, key = { it.reminder.id }) { dose ->
                AlarmDoseCard(
                    dose = dose,
                    showTime = doses.size > 1,
                    onTakeClick = { onTakeClick(dose) },
                    onSkipClick = { onSkipClick(dose) },
                    onSnoozeClick = { minutes -> onSnoozeClick(dose, minutes) }
                )
            }
        }

        if (doses.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onTakeAllClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.alarm_take_all_button), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun AlarmDoseCard(
    dose: AlarmDose,
    showTime: Boolean,
    onTakeClick: () -> Unit,
    onSkipClick: () -> Unit,
    onSnoozeClick: (minutes: Int) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dose.medicationName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (showTime) {
                val planned = timeFormat.format(Date(dose.reminder.timeInMillis))
                val rescheduledTo = dose.reminder.rescheduledToMillis
                Text(
                    text = if (rescheduledTo != null) {
                        stringResource(R.string.alarm_dose_rescheduled_label, planned, timeFormat.format(Date(rescheduledTo)))
                    } else {
                        stringResource(R.string.alarm_dose_time_label, planned)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            dose.dosage?.let {
                Text(
                    text = stringResource(R.string.alarm_dose_label, it),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onTakeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.alarm_took_it_button), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Üç düğme yan yana sığsın diye simgesiz ve tek satır.
                listOf(
                    stringResource(R.string.alarm_snooze_10_min) to { onSnoozeClick(10) },
                    stringResource(R.string.alarm_snooze_30_min) to { onSnoozeClick(30) },
                    stringResource(R.string.alarm_skip_short_button) to onSkipClick
                ).forEach { (label, onClick) ->
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                }
            }
        }
    }
}
