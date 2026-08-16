package com.gokcank.curalis.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gokcank.curalis.domain.model.Medication
import java.util.Calendar

/**
 * Rapor üretmeden önce tarih aralığını ve rapor içeriğini (ilaç filtresi + dahil edilecek
 * bölümler) seçtirir. İki adımdan oluşur: önce tarih aralığı, ardından içerik seçenekleri.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportOptionsDialog(
    medications: List<Medication>,
    onDismiss: () -> Unit,
    onConfirm: (
        startMillis: Long,
        endMillis: Long,
        medicationId: String?,
        includeAdherenceSummary: Boolean,
        includeMedicationList: Boolean,
        includeVitals: Boolean
    ) -> Unit
) {
    var showCustomRangePicker by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    if (selectedRange != null) {
        val (start, end) = selectedRange!!
        ReportContentOptionsDialog(
            medications = medications,
            onBack = { selectedRange = null },
            onDismiss = onDismiss,
            onConfirm = { medicationId, includeAdherence, includeMedList, includeVitals ->
                onConfirm(start, end, medicationId, includeAdherence, includeMedList, includeVitals)
            }
        )
        return
    }

    if (showCustomRangePicker) {
        val rangeState = rememberDateRangePickerState()
        Dialog(
            onDismissRequest = { showCustomRangePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
                    DateRangePicker(
                        state = rangeState,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomRangePicker = false }) {
                            Text("İptal")
                        }
                        TextButton(
                            onClick = {
                                val start = rangeState.selectedStartDateMillis
                                val end = rangeState.selectedEndDateMillis
                                if (start != null && end != null) {
                                    showCustomRangePicker = false
                                    selectedRange = start to endOfDay(end)
                                }
                            },
                            enabled = rangeState.selectedStartDateMillis != null && rangeState.selectedEndDateMillis != null
                        ) {
                            Text("Tamam")
                        }
                    }
                }
            }
        }
        return
    }

    val now = System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rapor Aralığı Seçin") },
        text = {
            Column {
                RangeOptionRow("Son 7 Gün") { selectedRange = now - daysInMillis(7) to now }
                RangeOptionRow("Son 30 Gün") { selectedRange = now - daysInMillis(30) to now }
                RangeOptionRow("Son 90 Gün") { selectedRange = now - daysInMillis(90) to now }
                RangeOptionRow("Bu Yıl") { selectedRange = startOfYear(now) to now }
                RangeOptionRow("Özel Aralık…") { showCustomRangePicker = true }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportContentOptionsDialog(
    medications: List<Medication>,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (
        medicationId: String?,
        includeAdherenceSummary: Boolean,
        includeMedicationList: Boolean,
        includeVitals: Boolean
    ) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMedicationId by remember { mutableStateOf<String?>(null) }
    var includeAdherence by remember { mutableStateOf(true) }
    var includeMedList by remember { mutableStateOf(true) }
    var includeVitals by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rapor İçeriği") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = medications.firstOrNull { it.id == selectedMedicationId }?.name ?: "Tüm İlaçlar",
                        onValueChange = {},
                        label = { Text("İlaç Filtresi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tüm İlaçlar") },
                            onClick = { selectedMedicationId = null; expanded = false }
                        )
                        medications.forEach { med ->
                            DropdownMenuItem(
                                text = { Text(med.name) },
                                onClick = { selectedMedicationId = med.id; expanded = false }
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("Dahil Edilecek Bölümler", style = MaterialTheme.typography.labelLarge)
                    ContentCheckboxRow("Uyum Özeti", includeAdherence) { includeAdherence = it }
                    ContentCheckboxRow("İlaç Dolabı Listesi", includeMedList) { includeMedList = it }
                    ContentCheckboxRow("Ölçümler Geçmişi", includeVitals) { includeVitals = it }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedMedicationId, includeAdherence, includeMedList, includeVitals) },
                enabled = includeAdherence || includeMedList || includeVitals
            ) {
                Text("Rapor Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text("Geri")
            }
        }
    )
}

@Composable
private fun ContentCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
private fun RangeOptionRow(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Start
        )
    }
}

private fun daysInMillis(days: Int): Long = days.toLong() * 24 * 3600 * 1000

private fun startOfYear(fromMillis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = fromMillis
    set(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** Seçilen bitiş günü, o günün tamamını (23:59:59.999'a kadar) kapsasın diye. */
private fun endOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis
