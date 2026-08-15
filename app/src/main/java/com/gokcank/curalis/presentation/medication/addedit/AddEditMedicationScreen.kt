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

    val context = LocalContext.current

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
            title = { Text("Saat değişikliği") },
            text = {
                Text(
                    "İlaç saatlerinde değişiklik yaptınız. Bugün için zaten planlanmış dozlar " +
                        "olduğu gibi kalsın mı, yoksa bu değişiklik hemen bugünden itibaren mi geçerli olsun?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showScheduleChangeScopeDialog = false
                    viewModel.confirmScheduleChangeScope(
                        AddEditMedicationViewModel.ScheduleChangeScope.FROM_NOW
                    )
                }) {
                    Text("Bugünden itibaren")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showScheduleChangeScopeDialog = false
                    viewModel.confirmScheduleChangeScope(
                        AddEditMedicationViewModel.ScheduleChangeScope.FROM_TOMORROW
                    )
                }) {
                    Text("Yarından itibaren")
                }
            }
        )
    }

    if (showPhotoChooser) {
        AlertDialog(
            onDismissRequest = { showPhotoChooser = false },
            title = { Text("İlaç Fotoğrafı") },
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
                        Text("Kameradan Çek", modifier = Modifier.fillMaxWidth())
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
                        Text("Galeriden Seç", modifier = Modifier.fillMaxWidth())
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
                                "Fotoğrafı Kaldır",
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
                        else stringResource(R.string.add_medication)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
                    supportingText = { Text("Yazdıkça resmî ilaç listesinde aranır") },
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.activeIngredient,
                onValueChange = viewModel::onActiveIngredientChange,
                label = { Text(stringResource(R.string.active_ingredient)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barkod: kutunun üzerindeki barkodu okutarak veya elle girerek kaydedilir.
            // TİTCK yerel veritabanında barkod eşlemesi bulunmadığı için otomatik ilaç
            // eşleştirmesi yapılmaz; yalnızca kullanıcının kendi kutusunu tanımasına yarar.
            val barcodeScanner = remember { com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context) }
            var barcodeError by remember { mutableStateOf<String?>(null) }

            OutlinedTextField(
                value = formState.barcode,
                onValueChange = viewModel::onBarcodeChange,
                label = { Text("Barkod (Opsiyonel)") },
                supportingText = barcodeError?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(onClick = {
                        barcodeError = null
                        barcodeScanner.startScan()
                            .addOnSuccessListener { result ->
                                viewModel.onBarcodeChange(result.rawValue ?: "")
                            }
                            .addOnFailureListener {
                                barcodeError = "Barkod okunamadı, elle girebilirsiniz."
                            }
                    }) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Barkod Tara")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

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
                        label = { Text(form.displayNameTr) },
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

            // 2.5 İlaç Renk Seçici (Color Picker)
            Text(text = "İlaç Renk Vurgusu", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MedicationAccentColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = formState.colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color = color, shape = CircleShape)
                            .clickable { viewModel.onColorSelected(hex) }
                            .semantics {
                                contentDescription = if (isSelected) "Seçili renk" else "Renk seçeneği"
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2.6 İlaç Fotoğrafı — yalnızca bu cihazda saklanır, hiçbir sunucuya yüklenmez.
            Text(text = "İlaç Fotoğrafı", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            MedicationPhotoPicker(
                photoPath = formState.photoPath,
                onClick = { showPhotoChooser = true }
            )

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
                        label = { Text("Birim") },
                        placeholder = { Text("Tablet / mg") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 3.5 Yemek Talimatı (Aç Karnına / Yemekle / Tok Karnına / Fark Etmez)
            Text(text = "Yemek Talimatı", style = MaterialTheme.typography.titleMedium)
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
                        label = { Text(instruction.displayNameTr) },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Sıklık / Periyot Seçimi
            Text(text = "İlaç Sıklığı / Kullanım Periyodu", style = MaterialTheme.typography.titleMedium)
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
                                FrequencyType.DAILY -> "Her Gün"
                                FrequencyType.SPECIFIC_DAYS -> "Haftanın Belirli Günleri"
                                FrequencyType.INTERVAL -> "X Günde Bir"
                                FrequencyType.CYCLIC -> "Döngüsel (Kullan / Ara Ver)"
                                FrequencyType.AS_NEEDED -> "İhtiyaç Halinde (PRN)"
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
                    label = { Text("Kaç günde bir alınacak?") },
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
                        label = { Text("Kaç gün kullanılacak?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = formState.restDays,
                        onValueChange = viewModel::onRestDaysChange,
                        label = { Text("Kaç gün ara verilecek?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Örn: 21 gün kullan, 7 gün ara ver — döngü başlangıç tarihinden itibaren tekrarlanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (formState.frequencyType == FrequencyType.SPECIFIC_DAYS) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gün Seçimi:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                val daysOfWeekNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
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
                Text(text = "Hatırlatıcı Saatleri", style = MaterialTheme.typography.titleMedium)
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
                                    text = formattedTime + (time.dose?.let { " • Doz: $it" } ?: ""),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            IconButton(onClick = { viewModel.removeMedicationTime(time.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
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
                    Text("Hatırlatıcı Saat Ekle")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Kutu / Stok Takibi (Stok Bitmeye Yakın Uyarı Bildirimi)
            Text(text = "Kutu / Stok Takibi", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stok Takibi Yapılsın mı?")
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
                        label = { Text("Mevcut Stok (Kutu/Tablet)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = formState.refillThreshold,
                        onValueChange = viewModel::onRefillThresholdChange,
                        label = { Text("Kritik Stok Uyarısı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (viewModel.isEditMode) {
                    val stockHistory by viewModel.stockHistory.collectAsState()
                    var showStockHistory by remember { mutableStateOf(false) }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showStockHistory = !showStockHistory }) {
                        Text(if (showStockHistory) "Stok Geçmişini Gizle" else "Stok Geçmişini Göster (${stockHistory.size})")
                    }
                    if (showStockHistory) {
                        if (stockHistory.isEmpty()) {
                            Text(
                                "Henüz bir stok değişikliği kaydedilmedi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                stockHistory.forEach { entry ->
                                    val reasonText = when (entry.reason) {
                                        StockChangeReason.DOSE_TAKEN -> "Doz alındı"
                                        StockChangeReason.MANUAL_EDIT -> "Elle düzenlendi"
                                        StockChangeReason.REFILL -> "Stok eklendi"
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

            Spacer(modifier = Modifier.height(16.dp))

            // 6.5 Doktor Bağlantısı (isteğe bağlı)
            val doctors by viewModel.doctors.collectAsState()
            var doctorMenuExpanded by remember { mutableStateOf(false) }
            val selectedDoctor = doctors.find { it.id == formState.doctorId }
            Text(text = "Doktor", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = doctorMenuExpanded,
                onExpandedChange = { doctorMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDoctor?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bu ilacı reçete eden doktor") },
                    placeholder = { Text("Belirtilmedi") },
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
                        text = { Text("Belirtilmedi") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Ek Bilgiler & Son Kullanma Tarihi
            Text(text = "Ek Bilgiler & Son Kullanma Tarihi", style = MaterialTheme.typography.titleMedium)
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
                } ?: "Son Kullanma Tarihi Seçin (Opsiyonel)"
                Text(formattedExpiry)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("İlaç Notları / Açıklama") },
                placeholder = { Text("Serin ve kuru yerde saklayınız.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = viewModel::saveMedication,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    if (viewModel.isEditMode) "Güncelle"
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
                    Text("Hatırlatıcı Saati Seçin", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showTimePickerDialog = false }) {
                            Text("İptal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.addMedicationTime(timePickerState.hour, timePickerState.minute)
                            showTimePickerDialog = false
                        }) {
                            Text("Ekle")
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
                    Text("Tamam")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePickerDialog = false }) {
                    Text("İptal")
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
                    Text(text = "Etken Madde: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text(text = suggestion.form ?: "İlaç", style = MaterialTheme.typography.labelSmall)
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
                contentDescription = "İlaç fotoğrafı",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Fotoğraf ekle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
