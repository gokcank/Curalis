package com.gokcank.curalis.presentation.medication.add_edit

import android.app.DatePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
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
    val name by viewModel.medicationName.collectAsState()
    val activeIngredient by viewModel.activeIngredient.collectAsState()
    val selectedFormType by viewModel.formType.collectAsState()
    val dosage by viewModel.medicationDosage.collectAsState()
    val unit by viewModel.medicationUnit.collectAsState()
    val mealInstruction by viewModel.mealInstruction.collectAsState()
    val medicationNotes by viewModel.medicationNotes.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val frequencyType by viewModel.frequencyType.collectAsState()
    val intervalDays by viewModel.intervalDays.collectAsState()
    val specificDays by viewModel.specificDays.collectAsState()
    val isRefillEnabled by viewModel.isRefillEnabled.collectAsState()
    val currentStock by viewModel.currentStock.collectAsState()
    val refillThreshold by viewModel.refillThreshold.collectAsState()
    val medicationTimes by viewModel.medicationTimes.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var pendingDoseText by remember { mutableStateOf("") }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditMedicationViewModel.UiEvent.SaveSuccess -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (viewModel.isEditMode) R.string.edit_medication else R.string.add_medication)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. İlaç Adı ve Otomatik Tamamlama
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(R.string.medication_name)) },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.width(20.dp))
                        }
                    },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Öneriler Listesi
            if (suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.suggestions_from_openfda),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(8.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = activeIngredient,
                onValueChange = viewModel::onActiveIngredientChange,
                label = { Text(stringResource(R.string.active_ingredient)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. İlaç Formu / Türü Seçici (💊, 🧪, 💉 vb.)
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
                        selected = selectedFormType == form,
                        onClick = { viewModel.onFormTypeChange(form) },
                        label = { Text("${form.iconEmoji} ${form.displayNameTr}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Doz & Birim Miktarı (Dropdown + Manuel Giriş)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dosage,
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
                        value = unit,
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
                            DropdownMenuItem(
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

            // 4. Yemek Talimatı Seçici (Aç / Tok / Yemekle / Fark Etmez)
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
                        selected = mealInstruction == instruction,
                        onClick = { viewModel.onMealInstructionChange(instruction) },
                        label = { Text("${instruction.iconEmoji} ${instruction.displayNameTr}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Kullanım Sıklığı (Frequency)
            Text(text = stringResource(R.string.frequency), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = frequencyType == FrequencyType.DAILY,
                    onClick = { viewModel.onFrequencyTypeChange(FrequencyType.DAILY) },
                    label = { Text(stringResource(R.string.freq_daily)) }
                )
                FilterChip(
                    selected = frequencyType == FrequencyType.SPECIFIC_DAYS,
                    onClick = { viewModel.onFrequencyTypeChange(FrequencyType.SPECIFIC_DAYS) },
                    label = { Text(stringResource(R.string.freq_specific_days)) }
                )
                FilterChip(
                    selected = frequencyType == FrequencyType.INTERVAL,
                    onClick = { viewModel.onFrequencyTypeChange(FrequencyType.INTERVAL) },
                    label = { Text(stringResource(R.string.freq_interval)) }
                )
                FilterChip(
                    selected = frequencyType == FrequencyType.AS_NEEDED,
                    onClick = { viewModel.onFrequencyTypeChange(FrequencyType.AS_NEEDED) },
                    label = { Text(stringResource(R.string.freq_as_needed)) }
                )
            }

            if (frequencyType == FrequencyType.INTERVAL) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = intervalDays,
                    onValueChange = viewModel::onIntervalDaysChange,
                    label = { Text(stringResource(R.string.interval_days_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (frequencyType == FrequencyType.SPECIFIC_DAYS) {
                Spacer(modifier = Modifier.height(8.dp))
                val daysOfWeek = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEachIndexed { index, dayName ->
                        val dayIndex = index + 1
                        FilterChip(
                            selected = specificDays.contains(dayIndex),
                            onClick = { viewModel.toggleSpecificDay(dayIndex) },
                            label = { Text(dayName) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Hatırlatıcı Saatleri (Çoklu Saat + Özel Dozaj)
            if (frequencyType != FrequencyType.AS_NEEDED) {
                Text(text = stringResource(R.string.reminder_times), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                medicationTimes.forEach { timeItem ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                val formattedTime = String.format("%02d:%02d", timeItem.hour, timeItem.minute)
                                val doseInfo = timeItem.dose?.let { " ($it)" } ?: ""
                                Text(text = "$formattedTime$doseInfo", style = MaterialTheme.typography.bodyLarge)
                            }
                            IconButton(onClick = { viewModel.removeMedicationTime(timeItem.id) }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.delete))
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

            // 7. Stok Takibi ve Yenileme Uyarısı
            Text(text = stringResource(R.string.stock_tracking), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.enable_stock_tracking))
                Switch(
                    checked = isRefillEnabled,
                    onCheckedChange = viewModel::onRefillToggle
                )
            }

            if (isRefillEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = viewModel::onCurrentStockChange,
                        label = { Text(stringResource(R.string.current_stock)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = refillThreshold,
                        onValueChange = viewModel::onRefillThresholdChange,
                        label = { Text(stringResource(R.string.refill_threshold)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Son Kullanma Tarihi ve Özel Notlar
            Text(text = "Ek Bilgiler & Son Kullanma Tarihi", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val formattedExpiry = expiryDate?.let {
                SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(Date(it))
            } ?: "Belirtilmedi (Tıkla ve Seç)"

            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    expiryDate?.let { cal.timeInMillis = it }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            viewModel.onExpiryDateChange(selectedCal.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Son Kullanma Tarihi: $formattedExpiry")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = medicationNotes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Serbest Not / Özel Talimatlar") },
                placeholder = { Text("Örn: Bol su ile yutulmalı") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveMedication,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTimePickerDialog) {
        TimeSelectionDialog(
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { hour, minute, dose ->
                viewModel.addMedicationTime(hour, minute, dose)
                showTimePickerDialog = false
            }
        )
    }
}

@Composable
fun SuggestionItem(
    suggestion: ProviderMedication,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = suggestion.name, style = MaterialTheme.typography.bodyMedium)
            if (!suggestion.activeIngredient.isNullOrBlank()) {
                Text(
                    text = suggestion.activeIngredient,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int, dose: String?) -> Unit
) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )
    var doseText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Saat & Doz Seçin", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = doseText,
                    onValueChange = { doseText = it },
                    label = { Text("Özel Dozaj Miktarı (İsteğe Bağlı)") },
                    placeholder = { Text("Örn: 1 Tablet veya 10 ml") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute, doseText)
                        }
                    ) {
                        Text("Ekle")
                    }
                }
            }
        }
    }
}
