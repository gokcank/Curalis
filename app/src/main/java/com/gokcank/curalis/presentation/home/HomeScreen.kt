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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.Whatshot
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
import com.gokcank.curalis.core.theme.ThemeMode
import com.gokcank.curalis.core.utils.PdfReportGeneratorEntryPoint
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.presentation.components.DoseTakenTimeDialog
import com.gokcank.curalis.presentation.components.icon
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

// Ana ekran renk kimliği: her bölümün kendi doygun rengi var, açık temada yumuşak bir
// gradyan zemin kullanılır. design-system.md'nin "dekoratif renk kullanılmaz" kuralına
// istisna — burada renk dekoratif değil, bölümü tanımlayan bir kimlik.
private val HomeGradientTop = Color(0xFFE9F1FE)
private val HomeGradientMid = Color(0xFFEAF6EE)
private val HomeGradientBottom = Color(0xFFFDF3E4)
private val HomeStreakFlame = Color(0xFFE8834A)
private val HomeAdherenceGreen = Color(0xFF2E7D6B)
private val HomeAccentMedications = Color(0xFF3B6FE0)
private val HomeAccentDoctors = Color(0xFF4F46E5)
private val HomeAccentAppointments = Color(0xFF9333EA)
private val HomeAccentVitals = Color(0xFFDC2626)
private val HomeAccentSymptoms = Color(0xFFD97706)
private val HomeAccentNotes = Color(0xFF0F766E)

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
        val themeMode by viewModel.themeMode.collectAsState()
        // Açık temada canlı, yumuşak bir gradyan zemin; koyu/AMOLED'de düz zemin kullanılır —
        // gradyan koyu temada iki ucu birbirine çok yakın olup 8-bit bantlanma yaratabilir ve
        // AMOLED'in pil avantajı düz siyahtan geliyor (bkz. eski gradyan yorumundaki gerekçe).
        val backgroundModifier = if (themeMode == ThemeMode.LIGHT) {
            Modifier.background(
                Brush.verticalGradient(
                    listOf(HomeGradientTop, HomeGradientMid, HomeGradientBottom)
                )
            )
        } else {
            Modifier.background(MaterialTheme.colorScheme.background)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
        ) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (uiState.streakDays > 0) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Whatshot,
                                        contentDescription = null,
                                        tint = HomeStreakFlame,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.home_streak_days, uiState.streakDays),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        uiState.monthlyAdherencePercentage?.let { pct ->
                            Text(
                                text = "%$pct",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = HomeAdherenceGreen,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                // Not: hasTodayReminders da dahil — günün son dozu geçmiş olsa bile "Bugünkü
                // Program" kartı kaybolmamalı, yoksa geçmiş bir dozu düzeltecek ekrana hiç
                // ulaşılamıyor (bkz. HomeUiState.hasTodayReminders).
                if (uiState.nextMedication != null || uiState.nextAppointment != null ||
                    uiState.latestVital != null || uiState.hasTodayReminders
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    uiState.nextMedication?.let { (reminder, med) ->
                        NextDoseHero(
                            medicationName = med?.name ?: stringResource(R.string.unknown),
                            timeMillis = reminder.timeInMillis,
                            onClick = { pendingDoseReminder = reminder }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (uiState.nextMedication == null) {
                        uiState.nextAppointment?.let { appt ->
                            val dateStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(appt.timeInMillis))
                            SummaryCard(
                                title = stringResource(R.string.upcoming_appointment),
                                content = "${appt.title} - $dateStr",
                                icon = Icons.Default.DateRange
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.home_other_section_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorDashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_medications),
                        subtitle = stringResource(R.string.home_active_medications_count, uiState.activeMedicationCount),
                        icon = Icons.AutoMirrored.Outlined.List,
                        containerColor = HomeAccentMedications,
                        onClick = onNavigateToMedications
                    )
                    ColorDashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_doctors),
                        subtitle = null,
                        icon = Icons.Default.Person,
                        containerColor = HomeAccentDoctors,
                        onClick = onNavigateToDoctors
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorDashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_appointments),
                        subtitle = uiState.nextAppointment?.let {
                            SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(it.timeInMillis))
                        },
                        icon = Icons.Default.DateRange,
                        containerColor = HomeAccentAppointments,
                        onClick = onNavigateToAppointments
                    )
                    ColorDashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_vitals),
                        subtitle = null,
                        icon = Icons.Default.Favorite,
                        containerColor = HomeAccentVitals,
                        onClick = onNavigateToVitals
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorDashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_symptoms),
                        subtitle = null,
                        icon = Icons.Default.Sick,
                        containerColor = HomeAccentSymptoms,
                        onClick = onNavigateToSymptoms
                    )
                    ColorDashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_daily_notes),
                        subtitle = null,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        containerColor = HomeAccentNotes,
                        onClick = onNavigateToDailyNotes
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
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

/**
 * Sıradaki dozu büyük, dairesel bir "ritüel" alanı olarak öne çıkarır (Round Health'ten
 * ilham) — kullanıcı bugünün listesinde arama yapmadan tek dokunuşla "aldım" diyebilsin diye.
 * Detaylı yönetim (atlama, erteleme) için Günlük Zaman Çizelgesi ekranı kullanılmaya devam eder.
 */
@Composable
fun NextDoseHero(
    medicationName: String,
    timeMillis: Long,
    onClick: () -> Unit
) {
    val timeStr = remember(timeMillis) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMillis)) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.home_next_up_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            border = BorderStroke(5.dp, HomeAccentMedications.copy(alpha = 0.18f)),
            modifier = Modifier.size(156.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(shape = RoundedCornerShape(16.dp), color = HomeAccentMedications, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.List,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = medicationName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.home_take_now_hint),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = HomeAccentMedications
        )
    }
}

/** Bölüme özgü doygun renkli, alt yazı gösterebilen ana ekran kutucuğu. */
@Composable
fun ColorDashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }
            }
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
                text = stringResource(R.string.placebo_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
