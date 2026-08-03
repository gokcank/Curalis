package com.gokcank.curalis.presentation.appointment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
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
            }
        }
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
