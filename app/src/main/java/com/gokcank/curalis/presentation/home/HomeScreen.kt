package com.gokcank.curalis.presentation.home

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
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.core.utils.PdfReportGeneratorEntryPoint
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.presentation.components.DoseTakenTimeDialog
import com.gokcank.curalis.presentation.components.icon
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gokcank.curalis.core.utils.PdfReportGenerator
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMedications: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToVitals: () -> Unit,
    onNavigateToSymptoms: () -> Unit = {},
    onNavigateToDailyNotes: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToDailyTimeline: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val medications by viewModel.medications.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val pdfReportGenerator = remember {
        val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
            context.applicationContext,
            PdfReportGeneratorEntryPoint::class.java
        )
        entryPoint.pdfReportGenerator()
    }
    var pdfPreviewFile by remember { mutableStateOf<java.io.File?>(null) }
    var reportSummary by remember { mutableStateOf<com.gokcank.curalis.core.utils.ReportSummary?>(null) }
    var showReportRangeDialog by remember { mutableStateOf(false) }
    var pendingDoseReminder by remember { mutableStateOf<Reminder?>(null) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_confirmation_title)) },
            text = { Text(stringResource(R.string.exit_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text(stringResource(R.string.exit), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.app_name), 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = { showReportRangeDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = stringResource(R.string.generate_pdf_report_content_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.calendar_content_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = stringResource(R.string.about),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Önceki dekoratif gradyan (surface -> background) açık gri temada iki ucu
                // birbirine çok yakın olduğu için 8-bit renk hassasiyetinde görünür bantlanma/
                // şerit çizgileri yaratıyordu. Düz bir zemin rengi bu riski tamamen ortadan
                // kaldırıyor; design-system.md zaten dekorasyonun okunabilirlikle
                // yarışmamasını istiyor, düz renk bu amaca da uyuyor.
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Dynamic Greeting
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 5..11 -> stringResource(R.string.greeting_morning)
                    in 12..17 -> stringResource(R.string.greeting_day)
                    in 18..22 -> stringResource(R.string.greeting_evening)
                    else -> stringResource(R.string.greeting_night)
                }
                
                val dateFormat = SimpleDateFormat("d MMMM EEEE", java.util.Locale.getDefault())
                val dateString = dateFormat.format(Date())

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Quick Glance Cards
                // Not: hasTodayReminders da dahil — günün son dozu geçmiş olsa bile "Bugünkü
                // Program" kartı kaybolmamalı, yoksa geçmiş bir dozu düzeltecek ekrana hiç
                // ulaşılamıyor (bkz. HomeUiState.hasTodayReminders).
                if (uiState.nextMedication != null || uiState.nextAppointment != null ||
                    uiState.latestVital != null || uiState.hasTodayReminders
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.nextMedication?.let { (reminder, med) ->
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(reminder.timeInMillis))
                            SummaryCard(
                                title = stringResource(R.string.next_medication),
                                content = "${med?.name ?: stringResource(R.string.unknown)} - $timeStr",
                                icon = Icons.AutoMirrored.Outlined.List,
                                onClick = onNavigateToDailyTimeline
                            )
                        }
                        
                        if (uiState.nextMedication == null) {
                            uiState.nextAppointment?.let { appt ->
                                val dateStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(appt.timeInMillis))
                                SummaryCard(
                                    title = stringResource(R.string.upcoming_appointment),
                                    content = "${appt.title} - $dateStr",
                                    icon = Icons.Default.DateRange
                                )
                            }
                        }
                        
                        // Daily Progress
                        if (uiState.hasTodayReminders) {
                            Card(
                                onClick = onNavigateToDailyTimeline,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.daily_progress), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Text(text = stringResource(R.string.medication_tracking), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = { uiState.dailyProgress },
                                            modifier = Modifier.size(48.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            strokeWidth = 6.dp
                                        )
                                        Text(
                                            text = "${(uiState.dailyProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.pillboxItems.isNotEmpty()) {
                            PillboxStrip(
                                items = uiState.pillboxItems,
                                onItemClick = { item ->
                                    val isTaken = item.reminder.state == ReminderState.TAKEN
                                    if (!isTaken) {
                                        pendingDoseReminder = item.reminder
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_medications),
                        icon = Icons.AutoMirrored.Outlined.List,
                        onClick = onNavigateToMedications
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_doctors),
                        icon = Icons.Default.Person,
                        onClick = onNavigateToDoctors
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_appointments),
                        icon = Icons.Default.DateRange,
                        onClick = onNavigateToAppointments
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_vitals),
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToVitals
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DashboardCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.dashboard_title_symptoms),
                    icon = Icons.Default.Sick,
                    onClick = onNavigateToSymptoms
                )

                Spacer(modifier = Modifier.height(16.dp))

                DashboardCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.dashboard_title_daily_notes),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    onClick = onNavigateToDailyNotes
                )
            }
        }
    }

    pdfPreviewFile?.let { file ->
        com.gokcank.curalis.presentation.components.PdfPreviewDialog(
            file = file,
            onDismiss = { pdfPreviewFile = null; reportSummary = null },
            onShare = { pdfReportGenerator.shareReport(context, file) },
            summary = reportSummary
        )
    }

    if (showReportRangeDialog) {
        com.gokcank.curalis.presentation.components.ReportOptionsDialog(
            medications = medications,
            onDismiss = { showReportRangeDialog = false },
            onConfirm = { start, end, medicationId, includeAdherence, includeMedList, includeVitals ->
                showReportRangeDialog = false
                scope.launch {
                    val result = pdfReportGenerator.generateReport(
                        startMillis = start,
                        endMillis = end,
                        medicationIds = medicationId?.let { setOf(it) },
                        includeAdherenceSummary = includeAdherence,
                        includeMedicationList = includeMedList,
                        includeVitals = includeVitals
                    )
                    pdfPreviewFile = result.file
                    reportSummary = result.summary
                }
            }
        )
    }

    pendingDoseReminder?.let { reminder ->
        DoseTakenTimeDialog(
            scheduledTimeMillis = reminder.timeInMillis,
            onDismiss = { pendingDoseReminder = null },
            onConfirm = { takenAtMillis ->
                viewModel.acknowledgeDose(reminder, ReminderState.TAKEN, takenAtMillis = takenAtMillis)
                pendingDoseReminder = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCard(title: String, content: String, icon: ImageVector, onClick: (() -> Unit)? = null) {
    Card(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = content, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * "Bugünün Kutusu" — Medisafe'in pillbox ana ekran görünümünden esinlenen, günün tüm
 * dozlarını küçük ilaç ikonları halinde tek bakışta gösteren şerit. Bekleyen (veya
 * atlanmış/kaçırılmış, düzeltilebilir) bir ikona dokunmak doğrudan "Aldım" akışını açar;
 * ayrı bir ekrana gitmeye gerek kalmaz. Detaylı yönetim (atlama, erteleme, geçmiş) için
 * Günlük Zaman Çizelgesi ekranı kullanılmaya devam eder.
 */
@Composable
fun PillboxStrip(
    items: List<HomePillboxItem>,
    onItemClick: (HomePillboxItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.todays_pillbox_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { it.reminder.id }) { item ->
                    PillboxDoseIcon(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
private fun PillboxDoseIcon(item: HomePillboxItem, onClick: () -> Unit) {
    val semantic = LocalCuralisColors.current
    val isTaken = item.reminder.state == ReminderState.TAKEN
    val isMissedOrSkipped = item.reminder.state == ReminderState.MISSED ||
        item.reminder.state == ReminderState.SKIPPED

    val tint = when {
        isTaken -> semantic.success
        isMissedOrSkipped -> semantic.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val containerAlpha = if (isTaken) 0.25f else 0.12f
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = containerAlpha),
            border = if (!isTaken) BorderStroke(1.5.dp, tint.copy(alpha = 0.5f)) else null,
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isTaken) Icons.Default.Check else (item.medication?.formType ?: MedicationForm.PILL).icon(),
                    contentDescription = item.medication?.name ?: stringResource(R.string.generic_medication_label),
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = timeFormat.format(Date(item.reminder.timeInMillis)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.reminder.isPlacebo) {
            Text(
                text = "Plasebo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
