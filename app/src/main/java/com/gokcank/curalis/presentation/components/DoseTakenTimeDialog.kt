package com.gokcank.curalis.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gokcank.curalis.R
import androidx.compose.ui.res.stringResource
import java.util.Calendar

private enum class DoseTakenTimeChoice { NOW, ON_TIME, PICK }

/**
 * Doz "Aldım" ile işaretlenirken ne zaman alındığını sorar — Şimdi / Tam zamanında / elle
 * seçilen bir saat. Bildirimdeki tek dokunuşluk "Al" eyleminde gösterilmez (SkipReasonDialog
 * ile aynı gerekçeyle: bildirim akışının hızı korunuyor); yalnızca uygulama içi Zaman
 * Çizelgesi ekranında kullanılır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseTakenTimeDialog(
    scheduledTimeMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (takenAtMillis: Long) -> Unit
) {
    var choice by remember { mutableStateOf(DoseTakenTimeChoice.NOW) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dose_taken_time_title)) },
        text = {
            Column {
                DoseTakenTimeOption(
                    label = stringResource(R.string.dose_taken_time_now),
                    selected = choice == DoseTakenTimeChoice.NOW,
                    onClick = { choice = DoseTakenTimeChoice.NOW }
                )
                DoseTakenTimeOption(
                    label = stringResource(R.string.dose_taken_time_on_time),
                    selected = choice == DoseTakenTimeChoice.ON_TIME,
                    onClick = { choice = DoseTakenTimeChoice.ON_TIME }
                )
                DoseTakenTimeOption(
                    label = stringResource(R.string.dose_taken_time_pick),
                    selected = choice == DoseTakenTimeChoice.PICK,
                    onClick = { choice = DoseTakenTimeChoice.PICK }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (choice) {
                    DoseTakenTimeChoice.NOW -> onConfirm(System.currentTimeMillis())
                    DoseTakenTimeChoice.ON_TIME -> onConfirm(scheduledTimeMillis)
                    DoseTakenTimeChoice.PICK -> showTimePicker = true
                }
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showTimePicker) {
        val initialCal = Calendar.getInstance().apply { timeInMillis = scheduledTimeMillis }
        val timePickerState = rememberTimePickerState(
            initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialCal.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.dose_taken_time_pick), style = MaterialTheme.typography.titleMedium)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TimePicker(state = timePickerState)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            val pickedCal = Calendar.getInstance().apply {
                                timeInMillis = scheduledTimeMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            showTimePicker = false
                            onConfirm(pickedCal.timeInMillis)
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseTakenTimeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}
