package com.gokcank.curalis.presentation.appointment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.presentation.components.ExactAlarmPermissionDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    viewModel: AddEditAppointmentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val location by viewModel.location.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val timeInMillis by viewModel.timeInMillis.collectAsState()
    val selectedDoctorId by viewModel.selectedDoctorId.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val isVisited by viewModel.isVisited.collectAsState()
    val visitNote by viewModel.visitNote.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    // Temporary holder for selected date before time is also picked
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(timeInMillis))

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditAppointmentViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is AddEditAppointmentViewModel.UiEvent.SaveSuccess -> {
                    onNavigateBack()
                }
                is AddEditAppointmentViewModel.UiEvent.ExactAlarmPermissionMissing -> {
                    showExactAlarmDialog = true
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (viewModel.isEditMode) R.string.edit_appointment else R.string.add_appointment)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.appointment_title)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = location,
                onValueChange = viewModel::onLocationChange,
                label = { Text(stringResource(R.string.appointment_location)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.appointment_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Doktor seçici — veri modeli/veritabanı ilişkisi zaten hazırdı, eksik olan
            // yalnızca bu seçim kutusuydu.
            var doctorMenuExpanded by remember { mutableStateOf(false) }
            val selectedDoctor = doctors.find { it.id == selectedDoctorId }
            ExposedDropdownMenuBox(
                expanded = doctorMenuExpanded,
                onExpandedChange = { doctorMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDoctor?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.appointment_doctor)) },
                    placeholder = { Text(stringResource(R.string.appointment_doctor_none)) },
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
                        text = { Text(stringResource(R.string.appointment_doctor_none)) },
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

            // Date & Time picker field — tapping opens DatePickerDialog
            OutlinedTextField(
                value = dateString,
                onValueChange = {},
                readOnly = true,
                label = { Text("${stringResource(R.string.date)} & ${stringResource(R.string.time)}") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Telefonun kendi Takvim uygulamasına etkinlik olarak eklemek için — birinci
            // taraf Takvim sağlayıcısı (CalendarContract), sunucuya hiçbir veri gitmez.
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                        putExtra(CalendarContract.Events.TITLE, title)
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeInMillis)
                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeInMillis + 60 * 60 * 1000L)
                        if (location.isNotBlank()) putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                        if (notes.isNotBlank()) putExtra(CalendarContract.Events.DESCRIPTION, notes)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Cihazda takvim uygulaması yoksa sessizce yok sayılır.
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Event, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_to_calendar))
            }

            if (viewModel.isEditMode) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = stringResource(R.string.visit_info), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVisited, onCheckedChange = viewModel::onVisitedChange)
                    Text(stringResource(R.string.visited))
                }
                if (isVisited) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = visitNote,
                        onValueChange = viewModel::onVisitNoteChange,
                        label = { Text(stringResource(R.string.visit_note)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveAppointment() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    // Step 1: Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true  // Move to time step
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Step 2: Time Picker Dialog (shown after date is picked)
    if (showTimePicker) {
        val currentCal = Calendar.getInstance().apply { setTimeInMillis(timeInMillis) }
        val timePickerState = rememberTimePickerState(
            initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentCal.get(Calendar.MINUTE),
            is24Hour = true
        )

        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.time),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { showTimePicker = false },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.cancel)) }
                        TextButton(
                            onClick = {
                                // Combine picked date + picked time into one millis value
                                val dateCal = Calendar.getInstance().apply {
                                    setTimeInMillis(selectedDateMillis ?: System.currentTimeMillis())
                                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    set(Calendar.MINUTE, timePickerState.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                viewModel.onTimeChange(dateCal.timeInMillis)
                                showTimePicker = false
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.ok)) }
                    }
                }
            }
        }
    }
}
