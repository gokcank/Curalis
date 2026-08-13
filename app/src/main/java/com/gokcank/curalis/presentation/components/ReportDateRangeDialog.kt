package com.gokcank.curalis.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import java.util.Calendar

/**
 * Rapor üretmeden önce hangi tarih aralığının kullanılacağını seçtirir. Hazır seçenekler
 * (son 7/30/90 gün) en yaygın ihtiyacı tek dokunuşla karşılar; "Özel aralık" ise
 * Material 3'ün kendi tarih aralığı seçicisini açar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDateRangeDialog(
    onDismiss: () -> Unit,
    onRangeSelected: (startMillis: Long, endMillis: Long) -> Unit
) {
    var showCustomRangePicker by remember { mutableStateOf(false) }

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
                                    onRangeSelected(start, endOfDay(end))
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
                RangeOptionRow("Son 7 Gün") { onRangeSelected(now - daysInMillis(7), now) }
                RangeOptionRow("Son 30 Gün") { onRangeSelected(now - daysInMillis(30), now) }
                RangeOptionRow("Son 90 Gün") { onRangeSelected(now - daysInMillis(90), now) }
                RangeOptionRow("Bu Yıl") { onRangeSelected(startOfYear(now), now) }
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
