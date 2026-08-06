package com.gokcank.curalis.presentation.medication.add_edit

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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.ProviderMedication
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

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

            // Doz & Birim Miktarı
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = viewModel::onDosageChange,
                    label = { Text(stringResource(R.string.medication_dosage)) },
                    placeholder = { Text("500") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = viewModel::onUnitChange,
                    label = { Text("Birim") },
                    placeholder = { Text("mg / ml") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Kullanım Sıklığı (Frequency)
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

            // 4. Hatırlatıcı Saatleri (Çoklu Saat + Özel Dozaj)
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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format("%02d:%02d", timeItem.hour, timeItem.minute),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                timeItem.dose?.let { dose ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "($dose)", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            IconButton(onClick = { viewModel.removeMedicationTime(timeItem.id) }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_reminder_time))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Kutu / Stok Takibi (Refill Alert)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(R.string.stock_tracking), style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = isRefillEnabled,
                            onCheckedChange = viewModel::onRefillToggle
                        )
                    }

                    if (isRefillEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = currentStock,
                                onValueChange = viewModel::onCurrentStockChange,
                                label = { Text(stringResource(R.string.current_stock)) },
                                placeholder = { Text("30") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = refillThreshold,
                                onValueChange = viewModel::onRefillThresholdChange,
                                label = { Text(stringResource(R.string.refill_threshold)) },
                                placeholder = { Text("5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveMedication,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(if (viewModel.isEditMode) R.string.save else R.string.add_medication))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Material 3 TimePicker Dialog + Dozaj Miktarı Girdisi
    if (showTimePickerDialog) {
        val calendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        Dialog(onDismissRequest = { showTimePickerDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.add_reminder_time), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    TimePicker(state = timePickerState)

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pendingDoseText,
                        onValueChange = { pendingDoseText = it },
                        label = { Text(stringResource(R.string.dose_amount)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showTimePickerDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.addMedicationTime(
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    pendingDoseText
                                )
                                pendingDoseText = ""
                                showTimePickerDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: ProviderMedication,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(text = suggestion.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        val details = listOfNotNull(suggestion.activeIngredient, suggestion.form).joinToString(" • ")
        if (details.isNotBlank()) {
            Text(text = details, style = MaterialTheme.typography.bodySmall)
        }
    }
}
