package com.gokcank.curalis.presentation.medication.addedit

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.MedicationAccentColors
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.presentation.components.ExactAlarmPermissionDialog
import com.gokcank.curalis.presentation.components.icon
import com.gokcank.curalis.domain.model.DosageUnit
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.ProviderMedication
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditMedicationViewModel = hiltViewModel()
) {
    // Formun tüm alanları tek bir state nesnesinde (bkz. MedicationFormState); arama
    // sonuçları/hata gibi geçici durumlar ayrı akışlarda kalır.
    val formState by viewModel.formState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Yeni ilaç eklerken zorunlu olmayan alanlar varsayılan olarak gizli kalır; kullanıcı
    // aşağıdaki "Neredeyse bitti" listesinden hangi ek bilgileri gireceğini işaretler.
    // Düzenleme modunda (viewModel.isEditMode) bu kısıtlama uygulanmaz — mevcut bir kaydın
    // alanları kullanıcıdan gizlenmez, hepsi her zaman gösterilir (bkz. aşağıdaki `showExtra`).
    var expandedExtraFields by remember { mutableStateOf(emptySet<String>()) }
    fun showExtra(key: String) = viewModel.isEditMode || key in expandedExtraFields

    val context = LocalContext.current
    // MedicationForm/MealInstruction display names are stored per-enum in both languages
    // (displayNameTr/displayNameEn) rather than as string resources — pick the right one here.
    val isEnglish = LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showScheduleChangeScopeDialog by remember { mutableStateOf(false) }

    var showPhotoChooser by remember { mutableStateOf(false) }
    var pendingCaptureFile by remember { mutableStateOf<java.io.File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCaptureFile
        if (success && file != null) {
            viewModel.onPhotoCaptured(file)
        }
        pendingCaptureFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onGalleryPhotoPicked(uri)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditMedicationViewModel.UiEvent.SaveSuccess -> {
                    onNavigateBack()
                }
                is AddEditMedicationViewModel.UiEvent.ExactAlarmPermissionMissing -> {
                    showExactAlarmDialog = true
                }
                is AddEditMedicationViewModel.UiEvent.ConfirmScheduleChangeScope -> {
                    showScheduleChangeScopeDialog = true
                }
            }
        }
    }

    if (showExactAlarmDialog) {
        ExactAlarmPermissionDialog(
            onDismiss = {
                showExactAlarmDialog = false
                onNavigateBack()
            }
        )
    }

    if (showScheduleChangeScopeDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleChangeScopeDialog = false },
            title = { Text(stringResource(R.string.schedule_change_dialog_title)) },
            text = {
                Text(stringResource(R.string.schedule_change_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = {
                    showScheduleChangeScopeDialog = false
                    viewModel.confirmScheduleChangeScope(
                        AddEditMedicationViewModel.ScheduleChangeScope.FROM_NOW
                    )
                }) {
                    Text(stringResource(R.string.schedule_change_from_now))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showScheduleChangeScopeDialog = false
                    viewModel.confirmScheduleChangeScope(
                        AddEditMedicationViewModel.ScheduleChangeScope.FROM_TOMORROW
                    )
                }) {
                    Text(stringResource(R.string.schedule_change_from_tomorrow))
                }
            }
        )
    }

    if (showPhotoChooser) {
        AlertDialog(
            onDismissRequest = { showPhotoChooser = false },
            title = { Text(stringResource(R.string.medication_photo_dialog_title)) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoChooser = false
                            val (file, uri) = viewModel.prepareCaptureTarget()
                            pendingCaptureFile = file
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.photo_take_camera), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            showPhotoChooser = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.photo_choose_gallery), modifier = Modifier.fillMaxWidth())
                    }
                    if (formState.photoPath != null) {
                        TextButton(
                            onClick = {
                                showPhotoChooser = false
                                viewModel.onRemovePhoto()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(R.string.photo_remove),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoChooser = false }) {
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
                        if (viewModel.isEditMode) stringResource(R.string.edit_medication)
                        else stringResource(R.string.add_medication),
                        color = com.gokcank.curalis.core.theme.SectionAccentMedications
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = com.gokcank.curalis.core.theme.SectionAccentMedications
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 1. İlaç Adı ve Autocomplete Arama
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(R.string.medication_name) + " *") },
                    supportingText = { Text(stringResource(R.string.medication_name_supporting_text)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                )

                if (suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = stringResource(R.string.suggested_ministry_of_health_medications),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(4.dp)
                            )
                            suggestions.take(4).forEach { suggestion ->
                                SuggestionItem(
                                    suggestion = suggestion,
                                    onClick = { viewModel.onSuggestionSelected(suggestion) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val isVerified = formState.isVerifiedSource
                Icon(
                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isVerified) stringResource(R.string.verified_source_badge) else stringResource(R.string.unverified_source_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showExtra("etken_madde")) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = formState.activeIngredient,
                    onValueChange = viewModel::onActiveIngredientChange,
                    label = { Text(stringResource(R.string.active_ingredient)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Barkod: kutunun üzerindeki barkodu okutarak veya elle girerek kaydedilir.
            // TİTCK yerel veritabanında barkod eşlemesi bulunmadığı için otomatik ilaç
            // eşleştirmesi yapılmaz; yalnızca kullanıcının kendi kutusunu tanımasına yarar.
            val barcodeScanner = remember { com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context) }
            var barcodeError by remember { mutableStateOf<String?>(null) }

            if (showExtra("barkod")) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = formState.barcode,
                    onValueChange = viewModel::onBarcodeChange,
                    label = { Text(stringResource(R.string.barcode_label)) },
                    supportingText = barcodeError?.let { { Text(it) } },
                    trailingIcon = {
                        val barcodeScanFailedText = stringResource(R.string.barcode_scan_failed)
                        IconButton(onClick = {
                            barcodeError = null
                            barcodeScanner.startScan()
                                .addOnSuccessListener { result ->
                                    viewModel.onBarcodeChange(result.rawValue ?: "")
                                }
                                .addOnFailureListener {
                                    barcodeError = barcodeScanFailedText
                                }
                        }) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = stringResource(R.string.barcode_scan_content_desc))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. İlaç Formu / Türü Seçici
            Text(text = stringResource(R.string.medication_form), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationForm.entries.forEach { form ->
                    FilterChip(
                        selected = formState.formType == form,
                        onClick = { viewModel.onFormTypeChange(form) },
                        label = { Text(if (isEnglish) form.displayNameEn else form.displayNameTr) },
                        leadingIcon = {
                            Icon(
                                imageVector = form.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MedicationLivePreview(
                name = formState.name,
                formType = formState.formType,
                dosage = formState.dosage,
                unit = formState.unit,
                colorHex = formState.colorHex
            )

            if (showExtra("renk")) {
                Spacer(modifier = Modifier.height(16.dp))

                // 2.5 İlaç Renk Seçici (Color Picker)
                Text(text = stringResource(R.string.medication_color_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val colorSelectedDesc = stringResource(R.string.color_selected_desc)
                    val colorOptionDesc = stringResource(R.string.color_option_desc)
                    MedicationAccentColors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = formState.colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color = color, shape = CircleShape)
                                .clickable { viewModel.onColorSelected(hex) }
                                .semantics {
                                    contentDescription = if (isSelected) colorSelectedDesc else colorOptionDesc
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showExtra("foto")) {
                Spacer(modifier = Modifier.height(16.dp))

                // 2.6 İlaç Fotoğrafı — yalnızca bu cihazda saklanır, hiçbir sunucuya yüklenmez.
                Text(text = stringResource(R.string.medication_photo_dialog_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                MedicationPhotoPicker(
                    photoPath = formState.photoPath,
                    onClick = { showPhotoChooser = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Doz & Birim Miktarı (Dropdown + Manuel Giriş)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.dosage,
                    onValueChange = viewModel::onDosageChange,
                    label = { Text(stringResource(R.string.medication_dosage)) },
                    placeholder = { Text("500") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded = isUnitDropdownExpanded,
                    onExpandedChange = { isUnitDropdownExpanded = !isUnitDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = formState.unit,
                        onValueChange = viewModel::onUnitChange,
                        label = { Text(stringResource(R.string.unit_label)) },
                        placeholder = { Text(stringResource(R.string.unit_placeholder)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isUnitDropdownExpanded,
                        onDismissRequest = { isUnitDropdownExpanded = false }
                    ) {
                        DosageUnit.ALL_UNITS.forEach { unitOption ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(unitOption) },
                                onClick = {
                                    viewModel.onUnitChange(unitOption)
                                    isUnitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (showExtra("yemek")) {
                Spacer(modifier = Modifier.height(16.dp))

                // 3.5 Yemek Talimatı (Aç Karnına / Yemekle / Tok Karnına / Fark Etmez)
                Text(text = stringResource(R.string.meal_instruction_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealInstruction.entries.forEach { instruction ->
                        FilterChip(
                            selected = formState.mealInstruction == instruction,
                            onClick = { viewModel.onMealInstructionChange(instruction) },
                            label = { Text(if (isEnglish) instruction.displayNameEn else instruction.displayNameTr) },
                            leadingIcon = {
                                Icon(
                                    imageVector = instruction.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Sıklık / Periyot Seçimi
            Text(text = stringResource(R.string.frequency_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrequencyType.entries.forEach { type ->
                    FilterChip(
                        selected = formState.frequencyType == type,
                        onClick = { viewModel.onFrequencyTypeChange(type) },
                        label = {
                            val text = when (type) {
                                FrequencyType.DAILY -> stringResource(R.string.frequency_daily)
                                FrequencyType.SPECIFIC_DAYS -> stringResource(R.string.frequency_specific_days)
                                FrequencyType.INTERVAL -> stringResource(R.string.frequency_interval)
                                FrequencyType.CYCLIC -> stringResource(R.string.frequency_cyclic)
                                FrequencyType.AS_NEEDED -> stringResource(R.string.frequency_as_needed)
                            }
                            Text(text)
                        }
                    )
                }
            }

            // Periyot detay seçicileri
            if (formState.frequencyType == FrequencyType.INTERVAL) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formState.intervalDays,
                    onValueChange = viewModel::onIntervalDaysChange,
                    label = { Text(stringResource(R.string.frequency_interval_days_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (formState.frequencyType == FrequencyType.CYCLIC) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formState.activeDays,
                        onValueChange = viewModel::onActiveDaysChange,
                        label = { Text(stringResource(R.string.cyclic_active_days_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = formState.restDays,
                        onValueChange = viewModel::onRestDaysChange,
                        label = { Text(stringResource(R.string.cyclic_rest_days_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    stringResource(R.string.cyclic_example_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.placebo_reminder_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.placebo_reminder_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = formState.hasPlaceboDays,
                        onCheckedChange = viewModel::onHasPlaceboDaysChange
                    )
                }
            }

            if (formState.frequencyType == FrequencyType.SPECIFIC_DAYS) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.day_selection_label), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                val daysOfWeekNames = androidx.compose.ui.res.stringArrayResource(R.array.weekday_short_names).toList()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeekNames.forEachIndexed { index, dayName ->
                        val dayNumber = index + 1
                        val isSelected = formState.specificDays.contains(dayNumber)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleSpecificDay(dayNumber) },
                            label = { Text(dayName) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Hatırlatıcı Saatleri Ekleme
            if (formState.frequencyType != FrequencyType.AS_NEEDED) {
                Text(text = stringResource(R.string.reminder_times_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                formState.times.forEach { time ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.minute)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formattedTime + (time.dose?.let { stringResource(R.string.dose_suffix_label, it) } ?: ""),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            IconButton(onClick = { viewModel.removeMedicationTime(time.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_reminder_time_button))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!viewModel.isEditMode) {
                // "Neredeyse bitti" listesi — zorunlu olmayan alanları tek uzun formdan
                // çıkarıp, kullanıcının isteğe bağlı olarak işaretleyebileceği bir listeye
                // taşır. İşaretlenen her öğe, o bölümü aşağıda görünür hale getirir.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.almost_done_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            stringResource(R.string.almost_done_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val extraFieldOptions = listOf(
                            "etken_madde" to stringResource(R.string.active_ingredient),
                            "barkod" to stringResource(R.string.barcode_field_label),
                            "renk" to stringResource(R.string.color_field_label),
                            "foto" to stringResource(R.string.photo_field_label),
                            "yemek" to stringResource(R.string.meal_instruction_title),
                            "stok" to stringResource(R.string.stock_tracking_field_label),
                            "doktor" to stringResource(R.string.doctor_field_label),
                            "ek_bilgi" to stringResource(R.string.expiry_notes_field_label)
                        )
                        // Kullanıcı bu alana daha önce bir değer girdiyse düğmede küçük bir
                        // onay işareti gösterilir — "Neredeyse bitti!" listesinde hangi ek
                        // bilgilerin zaten dolu olduğu tek bakışta belli olsun diye.
                        fun hasValue(key: String) = when (key) {
                            "etken_madde" -> formState.activeIngredient.isNotBlank()
                            "barkod" -> formState.barcode.isNotBlank()
                            "renk" -> formState.colorHex != "#1E88E5"
                            "foto" -> formState.photoPath != null
                            "yemek" -> formState.mealInstruction != MealInstruction.DOES_NOT_MATTER
                            "stok" -> formState.isRefillEnabled
                            "doktor" -> formState.doctorId != null
                            "ek_bilgi" -> formState.expiryDate != null ||
                                formState.notes.isNotBlank() ||
                                formState.treatmentDurationDays.isNotBlank() ||
                                formState.rxNumber.isNotBlank()
                            else -> false
                        }
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            extraFieldOptions.forEach { (key, label) ->
                                FilterChip(
                                    selected = key in expandedExtraFields,
                                    onClick = {
                                        expandedExtraFields = if (key in expandedExtraFields) {
                                            expandedExtraFields - key
                                        } else {
                                            expandedExtraFields + key
                                        }
                                    },
                                    label = { Text(label) },
                                    leadingIcon = if (hasValue(key)) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Kutu / Stok Takibi (Stok Bitmeye Yakın Uyarı Bildirimi)
            if (showExtra("stok")) {
            Text(text = stringResource(R.string.stock_tracking_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.stock_tracking_toggle_label))
                Switch(
                    checked = formState.isRefillEnabled,
                    onCheckedChange = viewModel::onRefillToggle
                )
            }

            if (formState.isRefillEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formState.currentStock,
                        onValueChange = viewModel::onCurrentStockChange,
                        label = { Text(stringResource(R.string.current_stock_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = formState.refillThreshold,
                        onValueChange = viewModel::onRefillThresholdChange,
                        label = { Text(stringResource(R.string.refill_threshold_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (viewModel.isEditMode) {
                    val stockHistory by viewModel.stockHistory.collectAsState()
                    var showStockHistory by remember { mutableStateOf(false) }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showStockHistory = !showStockHistory }) {
                        Text(if (showStockHistory) stringResource(R.string.stock_history_hide) else stringResource(R.string.stock_history_show, stockHistory.size))
                    }
                    if (showStockHistory) {
                        if (stockHistory.isEmpty()) {
                            Text(
                                stringResource(R.string.stock_history_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
                            val doseTakenReasonText = stringResource(R.string.dose_taken_reason)
                            val manualEditReasonText = stringResource(R.string.manual_edit_reason)
                            val refillReasonText = stringResource(R.string.refill_reason)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                stockHistory.forEach { entry ->
                                    val reasonText = when (entry.reason) {
                                        StockChangeReason.DOSE_TAKEN -> doseTakenReasonText
                                        StockChangeReason.MANUAL_EDIT -> manualEditReasonText
                                        StockChangeReason.REFILL -> refillReasonText
                                    }
                                    Text(
                                        text = "${dateFormat.format(Date(entry.timestamp))} — $reasonText: " +
                                            "${entry.previousStock ?: "-"} → ${entry.newStock ?: "-"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6.5 Doktor Bağlantısı (isteğe bağlı)
            if (showExtra("doktor")) {
            val doctors by viewModel.doctors.collectAsState()
            var doctorMenuExpanded by remember { mutableStateOf(false) }
            val selectedDoctor = doctors.find { it.id == formState.doctorId }
            Text(text = stringResource(R.string.doctor_field_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = doctorMenuExpanded,
                onExpandedChange = { doctorMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDoctor?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.prescribing_doctor_label)) },
                    placeholder = { Text(stringResource(R.string.not_specified)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = doctorMenuExpanded,
                    onDismissRequest = { doctorMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.not_specified)) },
                        onClick = {
                            viewModel.onDoctorSelected(null)
                            doctorMenuExpanded = false
                        }
                    )
                    doctors.forEach { doctor ->
                        DropdownMenuItem(
                            text = { Text(doctor.name) },
                            onClick = {
                                viewModel.onDoctorSelected(doctor.id)
                                doctorMenuExpanded = false
                            }
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Ek Bilgiler & Son Kullanma Tarihi
            if (showExtra("ek_bilgi")) {
            Text(text = stringResource(R.string.additional_info_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDatePickerDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val formattedExpiry = formState.expiryDate?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                } ?: stringResource(R.string.expiry_date_picker_placeholder)
                Text(formattedExpiry)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.medication_notes_label)) },
                placeholder = { Text(stringResource(R.string.medication_notes_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.treatmentDurationDays,
                onValueChange = viewModel::onTreatmentDurationChange,
                label = { Text(stringResource(R.string.treatment_duration_label)) },
                placeholder = { Text(stringResource(R.string.treatment_duration_placeholder)) },
                supportingText = { Text(stringResource(R.string.treatment_duration_supporting_text)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.rxNumber,
                onValueChange = viewModel::onRxNumberChange,
                label = { Text(stringResource(R.string.rx_number_label)) },
                placeholder = { Text(stringResource(R.string.rx_number_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = viewModel::saveMedication,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.gokcank.curalis.core.theme.SectionAccentMedications,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text(
                    if (viewModel.isEditMode) stringResource(R.string.update_button)
                    else stringResource(R.string.save)
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
        Dialog(onDismissRequest = { showTimePickerDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.reminder_time_picker_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showTimePickerDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.addMedicationTime(timePickerState.hour, timePickerState.minute)
                            showTimePickerDialog = false
                        }) {
                            Text(stringResource(R.string.add))
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog (Son Kullanma Tarihi)
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onExpiryDateChange(it)
                    }
                    showDatePickerDialog = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePickerDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: ProviderMedication,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = suggestion.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                suggestion.activeIngredient?.let {
                    Text(text = stringResource(R.string.active_ingredient_prefix, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text(text = suggestion.form ?: stringResource(R.string.generic_medication_label), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MedicationPhotoPicker(photoPath: String?, onClick: () -> Unit) {
    // Bitmap'i disk yolundan yalnızca yol değiştiğinde yeniden okur — her yeniden
    // kompozisyonda dosyayı tekrar açmak gereksiz bir G/Ç maliyeti olurdu.
    val imageBitmap = remember(photoPath) {
        photoPath?.let { path ->
            runCatching { android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = stringResource(R.string.medication_photo_content_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = stringResource(R.string.add_photo_content_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Seçilen ilaç şekli ve rengin, listede/kutuda nasıl görüneceğini kullanıcıya baştan
 * gösteren büyük, canlı güncellenen önizleme. Form/renk/isim/doz değiştikçe anında
 * yeniden çizilir; ayrı bir onay adımı gerektirmez.
 */
@Composable
private fun MedicationLivePreview(
    name: String,
    formType: MedicationForm,
    dosage: String,
    unit: String,
    colorHex: String
) {
    val accentColor = remember(colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color(0xFF1E88E5))
    }
    val dosageLine = listOfNotNull(dosage.takeIf { it.isNotBlank() }, unit.takeIf { it.isNotBlank() })
        .joinToString(" ")
    val isEnglish = LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(color = accentColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = formType.icon(),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name.ifBlank { stringResource(R.string.medication_name_placeholder) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = listOfNotNull(if (isEnglish) formType.displayNameEn else formType.displayNameTr, dosageLine.takeIf { it.isNotBlank() }).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
