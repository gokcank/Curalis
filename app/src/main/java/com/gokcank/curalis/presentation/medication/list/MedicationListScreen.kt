package com.gokcank.curalis.presentation.medication.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.rememberCoroutineScope
import com.gokcank.curalis.core.utils.PdfReportGeneratorEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.core.theme.SectionAccentMedications
import com.gokcank.curalis.core.theme.rememberSectionThemeMode
import com.gokcank.curalis.core.theme.sectionBackgroundModifier
import com.gokcank.curalis.core.utils.PdfReportGenerator
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.presentation.components.EmptyState
import com.gokcank.curalis.presentation.components.InfoChip
import com.gokcank.curalis.presentation.components.NoResultsState
import com.gokcank.curalis.presentation.components.icon
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onNavigateToAddEdit: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToStockHistory: () -> Unit,
    viewModel: MedicationListViewModel = hiltViewModel()
) {
    val activeMedications by viewModel.activeMedications.collectAsState()
    val archivedMedications by viewModel.archivedMedications.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    val pdfReportGenerator = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PdfReportGeneratorEntryPoint::class.java
        )
        entryPoint.pdfReportGenerator()
    }
    var pdfPreviewFile by remember { mutableStateOf<java.io.File?>(null) }
    var reportSummary by remember { mutableStateOf<com.gokcank.curalis.core.utils.ReportSummary?>(null) }
    var showReportOptionsDialog by remember { mutableStateOf(false) }

    medicationToDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { medicationToDelete = null },
            title = { Text(stringResource(R.string.delete_medication_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.delete_medication_message, med.name))
                    Spacer(modifier = Modifier.height(8.dp))
                    // Yıkıcı "hepsini sil" eylemi kasıtlı olarak diyaloğun sistem
                    // "vazgeç" (dismissButton) slotunda değil, ayrı ve bariz bir yerde
                    // duruyor — dismissButton'ın gerçekten yalnızca iptal anlamına
                    // gelmesi için.
                    TextButton(
                        onClick = {
                            viewModel.deleteMedication(med)
                            medicationToDelete = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.delete_medication_all_button), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                // Zaten arşivde olan bir ilaç için "arşivle" seçeneğinin bir anlamı
                // yok — yalnızca Aktif sekmesinden gelen silme akışında gösterilir.
                if (!med.isArchived) {
                    TextButton(
                        onClick = {
                            viewModel.archiveMedication(med)
                            medicationToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.delete_medication_archive_button))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val themeMode by rememberSectionThemeMode()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_medications), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStockHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.refill_history_content_desc)
                        )
                    }
                    IconButton(onClick = { showReportOptionsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = stringResource(R.string.generate_pdf_report_content_desc)
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(null) },
                containerColor = SectionAccentMedications,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(sectionBackgroundModifier(themeMode))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.medication_name)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.tab_active)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.tab_archive, archivedMedications.size)) }
                )
            }

            val visibleMedications = if (selectedTab == 0) activeMedications else archivedMedications

            if (visibleMedications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (searchQuery.isNotBlank()) {
                        // Kayıtlar var, yalnızca arama eşleşmedi: "hiç ilaç eklemediniz"
                        // demek yanlış bilgi olurdu.
                        NoResultsState(
                            icon = Icons.Default.Search,
                            query = searchQuery,
                            onClearSearch = { viewModel.onSearchQueryChange("") }
                        )
                    } else if (selectedTab == 1) {
                        EmptyState(
                            icon = Icons.Default.Inventory2,
                            title = stringResource(R.string.no_archived_medications_title),
                            description = stringResource(R.string.no_archived_medications_description)
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Default.Medication,
                            title = stringResource(R.string.no_medications_found),
                            description = stringResource(R.string.no_medications_description),
                            actionLabel = stringResource(R.string.add_medication),
                            onAction = { onNavigateToAddEdit(null) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleMedications) { medication ->
                        if (selectedTab == 1) {
                            ArchivedMedicationItem(
                                medication = medication,
                                onRestore = { viewModel.unarchiveMedication(medication) },
                                onDeleteForever = { medicationToDelete = medication }
                            )
                        } else {
                            MedicationItem(
                                medication = medication,
                                onClick = { onNavigateToAddEdit(medication.id) },
                                onTakeDose = { viewModel.takeDose(medication.id) },
                                onDelete = { medicationToDelete = medication },
                                onToggleSuspend = {
                                    if (medication.isSuspended) {
                                        viewModel.resumeMedication(medication)
                                    } else {
                                        viewModel.suspendMedication(medication)
                                    }
                                }
                            )
                        }
                    }
                }
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

    if (showReportOptionsDialog) {
        com.gokcank.curalis.presentation.components.ReportOptionsDialog(
            medications = activeMedications + archivedMedications,
            onDismiss = { showReportOptionsDialog = false },
            onConfirm = { start, end, medicationId, includeAdherence, includeMedList, includeVitals ->
                showReportOptionsDialog = false
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
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationItem(
    medication: Medication,
    onClick: () -> Unit,
    onTakeDose: () -> Unit,
    onDelete: () -> Unit,
    onToggleSuspend: () -> Unit
) {
    // MedicationForm/MealInstruction display names are stored per-enum in both languages
    // (displayNameTr/displayNameEn) rather than as string resources — pick the right one here.
    val isEnglish = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val customColor = try {
                Color(android.graphics.Color.parseColor(medication.colorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primaryContainer
            }

            Surface(
                shape = CircleShape,
                color = customColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = medication.formType.icon(),
                        contentDescription = if (isEnglish) medication.formType.displayNameEn else medication.formType.displayNameTr,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                val dosageStr = listOfNotNull(medication.dosage, medication.unit).joinToString(" ")
                val subtitleParts = listOfNotNull(
                    medication.activeIngredient.takeIf { !it.isNullOrBlank() },
                    dosageStr.takeIf { it.isNotBlank() },
                    if (isEnglish) medication.formType.displayNameEn else medication.formType.displayNameTr
                ).joinToString(" • ")

                if (subtitleParts.isNotBlank()) {
                    Text(
                        text = subtitleParts,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (medication.isSuspended) {
                        val semantic = LocalCuralisColors.current
                        InfoChip(
                            icon = Icons.Default.PauseCircle,
                            text = stringResource(R.string.suspended_chip_label),
                            containerColor = semantic.warningContainer,
                            contentColor = semantic.onWarningContainer
                        )
                    }

                    medication.treatmentDurationDays?.let { days ->
                        val elapsedDays = ((System.currentTimeMillis() - medication.startDate) / (24 * 60 * 60 * 1000L)).toInt()
                        val remaining = days - elapsedDays
                        InfoChip(
                            icon = Icons.Default.Event,
                            text = if (remaining > 0) stringResource(R.string.treatment_days_remaining_chip, remaining) else stringResource(R.string.treatment_ended_chip),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    InfoChip(
                        icon = if (medication.isVerifiedSource) Icons.Default.Verified else Icons.Default.Edit,
                        text = if (medication.isVerifiedSource) stringResource(R.string.verified_source_chip) else stringResource(R.string.manually_entered_chip),
                        containerColor = if (medication.isVerifiedSource) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (medication.isVerifiedSource) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (medication.times.isNotEmpty()) {
                        val timesStr = medication.times.joinToString(", ") {
                            String.format(Locale.getDefault(), "%02d:%02d", it.hour, it.minute)
                        }
                        InfoChip(
                            icon = Icons.Default.Schedule,
                            text = timesStr,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    if (medication.mealInstruction != MealInstruction.DOES_NOT_MATTER) {
                        InfoChip(
                            icon = medication.mealInstruction.icon(),
                            text = if (isEnglish) medication.mealInstruction.displayNameEn else medication.mealInstruction.displayNameTr,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val freqText = when (medication.frequencyType) {
                        FrequencyType.DAILY -> stringResource(R.string.freq_every_day_chip)
                        FrequencyType.SPECIFIC_DAYS -> stringResource(R.string.freq_specific_days_chip)
                        FrequencyType.INTERVAL -> stringResource(R.string.freq_every_x_days_chip, medication.intervalDays ?: 2)
                        FrequencyType.CYCLIC -> stringResource(R.string.freq_cyclic_chip, medication.activeDays ?: 21, medication.restDays ?: 7)
                        FrequencyType.AS_NEEDED -> stringResource(R.string.freq_as_needed_chip)
                    }
                    InfoChip(
                        icon = Icons.Default.Event,
                        text = freqText,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    medication.currentStock?.let { stock ->
                        if (medication.isRefillReminderEnabled) {
                            val isLowStock = stock <= (medication.refillThreshold ?: 5)
                            // Düşük stok bir hata değil, dikkat gerektiren bir durumdur:
                            // design-system.md Error rengini sıradan dikkat için yasaklıyor.
                            val semantic = LocalCuralisColors.current
                            InfoChip(
                                icon = if (isLowStock) Icons.Default.Warning else Icons.Default.Inventory2,
                                text = if (isLowStock) stringResource(R.string.stock_low_chip, stock) else stringResource(R.string.stock_remaining_chip, stock),
                                containerColor = if (isLowStock) semantic.warningContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isLowStock) semantic.onWarningContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (medication.frequencyType == FrequencyType.AS_NEEDED) {
                IconButton(onClick = onTakeDose) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.take_now_content_desc),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onToggleSuspend) {
                Icon(
                    imageVector = if (medication.isSuspended) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                    contentDescription = if (medication.isSuspended) stringResource(R.string.resume_content_desc) else stringResource(R.string.suspend_content_desc),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ArchivedMedicationItem(
    medication: Medication,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val dosageStr = listOfNotNull(medication.dosage, medication.unit).joinToString(" ")
                if (dosageStr.isNotBlank()) {
                    Text(
                        text = dosageStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = stringResource(R.string.unarchive_content_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteForever) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
