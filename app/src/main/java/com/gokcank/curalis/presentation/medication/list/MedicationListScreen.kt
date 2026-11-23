package com.gokcank.curalis.presentation.medication.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.core.theme.SectionAccentMedications
import com.gokcank.curalis.core.theme.rememberSectionThemeMode
import com.gokcank.curalis.core.theme.sectionBackgroundModifier
import com.gokcank.curalis.core.utils.PdfReportGenerator
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.usecase.FrequencyCalculator
import com.gokcank.curalis.presentation.components.EmptyState
import com.gokcank.curalis.presentation.components.InfoChip
import com.gokcank.curalis.presentation.components.NoResultsState
import com.gokcank.curalis.presentation.components.icon
import com.gokcank.curalis.presentation.components.shapeIcon
import android.content.Intent
import androidx.compose.material.icons.filled.Share
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
    var medicationToEnd by remember { mutableStateOf<Medication?>(null) }
    var stockDialogFor by remember { mutableStateOf<Medication?>(null) }

    stockDialogFor?.let { med ->
        StockShortcutDialog(
            medication = med,
            onAddBox = {
                viewModel.addStockBox(med)
                stockDialogFor = null
            },
            onSetStock = { value ->
                viewModel.setStock(med, value)
                stockDialogFor = null
            },
            onDismiss = { stockDialogFor = null }
        )
    }
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

    medicationToEnd?.let { med ->
        AlertDialog(
            onDismissRequest = { medicationToEnd = null },
            title = { Text(stringResource(R.string.end_treatment_dialog_title)) },
            text = { Text(stringResource(R.string.end_treatment_dialog_message, med.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Tedaviyi bitirmek arşivlemektir: hatırlatmalar ve gelecek dozlar kalkar,
                        // geçmiş korunur, ilaç Arşiv sekmesinden geri alınabilir.
                        viewModel.archiveMedication(med)
                        medicationToEnd = null
                    }
                ) {
                    Text(stringResource(R.string.end_treatment_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationToEnd = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val themeMode by rememberSectionThemeMode()

    // İlaç listesini düz metin olarak paylaşma (bkz. MedicationShareText).
    val isEnglishLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val shareLabels = MedicationShareText.Labels(
        title = stringResource(
            R.string.share_list_title,
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(locale))
        ),
        times = stringResource(R.string.share_list_times),
        frequency = stringResource(R.string.share_list_frequency),
        meal = stringResource(R.string.share_list_meal)
    )
    val dailyLabel = stringResource(R.string.freq_every_day_chip)
    val asNeededLabel = stringResource(R.string.freq_as_needed_chip)
    val intervalPattern = stringResource(R.string.freq_every_x_days_chip)
    val cyclicPattern = stringResource(R.string.freq_cyclic_chip)
    val frequencyLabel: (Medication) -> String = { med ->
        when (med.frequencyType) {
            FrequencyType.DAILY -> dailyLabel
            FrequencyType.SPECIFIC_DAYS -> med.specificDays.sorted().joinToString(", ") { day ->
                java.time.DayOfWeek.of(day).getDisplayName(java.time.format.TextStyle.SHORT, locale)
            }
            FrequencyType.INTERVAL -> String.format(locale, intervalPattern, med.intervalDays ?: 2)
            FrequencyType.CYCLIC -> String.format(locale, cyclicPattern, med.activeDays ?: 21, med.restDays ?: 7)
            FrequencyType.AS_NEEDED -> asNeededLabel
        }
    }
    val mealLabel: (MealInstruction) -> String = { if (isEnglishLocale) it.displayNameEn else it.displayNameTr }
    val shareEmptyMessage = stringResource(R.string.share_list_empty)
    val shareChooserTitle = stringResource(R.string.share_list_action)

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
                    IconButton(onClick = {
                        scope.launch {
                            val meds = viewModel.medicationsForSharing()
                            if (meds.isEmpty()) {
                                snackbarHostState.showSnackbar(shareEmptyMessage)
                            } else {
                                val text = MedicationShareText.build(meds, shareLabels, frequencyLabel, mealLabel)
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(send, shareChooserTitle))
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_list_action)
                        )
                    }
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
                placeholder = { Text(stringResource(R.string.medication_list_search_hint)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            SecondaryTabRow(selectedTabIndex = selectedTab) {
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
                    items(visibleMedications, key = { it.id }) { medication ->
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
                                onEndTreatment = { medicationToEnd = medication },
                                onStockClick = { stockDialogFor = medication },
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
            onConfirm = { start, end, medicationId, includeAdherence, includeMedList, includeVitals, language ->
                showReportOptionsDialog = false
                scope.launch {
                    val result = pdfReportGenerator.generateReport(
                        startMillis = start,
                        endMillis = end,
                        medicationIds = medicationId?.let { setOf(it) },
                        includeAdherenceSummary = includeAdherence,
                        includeMedicationList = includeMedList,
                        includeVitals = includeVitals,
                        language = language
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
    onToggleSuspend: () -> Unit,
    onEndTreatment: () -> Unit,
    onStockClick: () -> Unit = {}
) {
    // MedicationForm/MealInstruction display names are stored per-enum in both languages
    // (displayNameTr/displayNameEn) rather than as string resources — pick the right one here.
    val isEnglish = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language
    // Kart kapalıyken yalnızca saatler, sıklık ve kaynak görünür; geri kalan bilgiler ve
    // düzenleme düğmesi karta dokununca açılır.
    var expanded by rememberSaveable(medication.id) { mutableStateOf(false) }
    val toggleLabel = stringResource(if (expanded) R.string.hide_details else R.string.show_details)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClickLabel = toggleLabel,
                onClick = { expanded = !expanded },
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
                        imageVector = medication.shapeIcon(),
                        // Form adı kartta zaten yazıyor; simge ayrıca okunursa iki kez duyulur.
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(if (expanded) R.string.hide_details else R.string.show_details),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

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

                // Durum etiketleri (askıda, tedavi bitti) kart kapalıyken de görünür.
                val remainingTreatmentDays = FrequencyCalculator.remainingTreatmentDays(medication)
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

                    if (remainingTreatmentDays == 0) {
                        val semantic = LocalCuralisColors.current
                        InfoChip(
                            icon = Icons.Default.EventBusy,
                            text = stringResource(R.string.treatment_ended_chip),
                            containerColor = semantic.warningContainer,
                            contentColor = semantic.onWarningContainer
                        )
                    }

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

                    // Kartta kısa etiket; ekran okuyucu tam adını okur.
                    val sourceDescription = stringResource(
                        if (medication.isVerifiedSource) R.string.verified_source_desc else R.string.manually_entered_desc
                    )
                    InfoChip(
                        icon = if (medication.isVerifiedSource) Icons.Default.Verified else Icons.Default.Edit,
                        text = if (medication.isVerifiedSource) stringResource(R.string.verified_source_chip) else stringResource(R.string.manually_entered_chip),
                        modifier = Modifier.clearAndSetSemantics { contentDescription = sourceDescription },
                        containerColor = if (medication.isVerifiedSource) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (medication.isVerifiedSource) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (remainingTreatmentDays != null && remainingTreatmentDays > 0) {
                                InfoChip(
                                    icon = Icons.Default.Event,
                                    text = stringResource(R.string.treatment_days_remaining_chip, remainingTreatmentDays),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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

                            medication.currentStock?.let { stock ->
                                if (medication.isRefillReminderEnabled) {
                                    val isLowStock = stock <= (medication.refillThreshold ?: 5)
                                    // Düşük stok bir hata değil, dikkat gerektiren bir durumdur:
                                    // design-system.md Error rengini sıradan dikkat için yasaklıyor.
                                    val semantic = LocalCuralisColors.current
                                    // Dokununca "Bir kutu ekle" / "Stok gir" penceresi açılır.
                                    InfoChip(
                                        icon = if (isLowStock) Icons.Default.Warning else Icons.Default.Inventory2,
                                        text = if (isLowStock) stringResource(R.string.stock_low_chip, stock) else stringResource(R.string.stock_remaining_chip, stock),
                                        containerColor = if (isLowStock) semantic.warningContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isLowStock) semantic.onWarningContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable(
                                            onClickLabel = stringResource(R.string.stock_change_action),
                                            role = androidx.compose.ui.semantics.Role.Button,
                                            onClick = onStockClick
                                        )
                                    )
                                }
                            }
                        }
                        // Düzenleme, askıya alma ve silme sık kullanılmadığı için burada durur;
                        // kapalı kart böylece tek satıra sığar.
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CardActionButton(Icons.Default.Edit, stringResource(R.string.edit_short), onClick)
                            CardActionButton(
                                icon = if (medication.isSuspended) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                                label = if (medication.isSuspended) stringResource(R.string.resume_content_desc) else stringResource(R.string.suspend_content_desc),
                                onClick = onToggleSuspend
                            )
                            CardActionButton(Icons.Default.Flag, stringResource(R.string.end_treatment_action), onEndTreatment)
                            CardActionButton(Icons.Default.Delete, stringResource(R.string.delete), onDelete)
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
        }
    }
}

@Composable
private fun CardActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
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

/**
 * Stok etiketine dokununca açılan kısa yol: bir kutu (başlangıç stoğu kadar) eklemek ya da
 * stoğu elle girmek. Başlangıç stoğu yoksa yalnızca elle giriş gösterilir.
 */
@Composable
private fun StockShortcutDialog(
    medication: Medication,
    onAddBox: () -> Unit,
    onSetStock: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var entered by remember { mutableStateOf("") }
    val boxSize = medication.initialStock?.takeIf { it > 0 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(medication.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.stock_current, medication.currentStock ?: 0))
                if (boxSize != null) {
                    androidx.compose.material3.FilledTonalButton(onClick = onAddBox, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.stock_add_box, boxSize))
                    }
                }
                OutlinedTextField(
                    value = entered,
                    onValueChange = { value -> entered = value.filter { it.isDigit() }.take(5) },
                    label = { Text(stringResource(R.string.stock_enter_label)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { entered.toIntOrNull()?.let(onSetStock) },
                enabled = entered.toIntOrNull() != null
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
