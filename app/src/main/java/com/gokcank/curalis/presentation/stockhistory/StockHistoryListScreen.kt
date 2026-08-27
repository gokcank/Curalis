package com.gokcank.curalis.presentation.stockhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.presentation.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockHistoryListScreen(
    viewModel: StockHistoryListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val rows by viewModel.rows.collectAsState()
    val medicationNames by viewModel.medicationNames.collectAsState()
    val selectedMedicationId by viewModel.selectedMedicationId.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.refill_history_content_desc), color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val allMedicationsLabel = stringResource(R.string.all_medications)
                OutlinedTextField(
                    readOnly = true,
                    value = medicationNames.firstOrNull { it.first == selectedMedicationId }?.second ?: allMedicationsLabel,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.medication_filter_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(allMedicationsLabel) },
                        onClick = {
                            viewModel.onMedicationFilterSelected(null)
                            expanded = false
                        }
                    )
                    medicationNames.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                viewModel.onMedicationFilterSelected(id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.stock_history_no_records_title),
                        description = stringResource(R.string.stock_history_no_records_desc)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows) { row ->
                        StockHistoryRowItem(row = row)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockHistoryRowItem(row: StockHistoryRow) {
    val semantic = LocalCuralisColors.current
    val displayLocale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(displayLocale) { SimpleDateFormat("dd MMM yyyy, HH:mm", displayLocale) }

    val (icon, tint) = when (row.entry.reason) {
        StockChangeReason.REFILL -> Icons.Default.AddCircle to semantic.success
        StockChangeReason.DOSE_TAKEN -> Icons.Default.CheckCircle to semantic.info
        StockChangeReason.MANUAL_EDIT -> Icons.Default.Edit to semantic.warning
    }
    val reasonText = when (row.entry.reason) {
        StockChangeReason.REFILL -> stringResource(R.string.reason_refill)
        StockChangeReason.DOSE_TAKEN -> stringResource(R.string.reason_dose_taken)
        StockChangeReason.MANUAL_EDIT -> stringResource(R.string.reason_manual_edit)
    }
    val changeText = if (row.entry.previousStock != null && row.entry.newStock != null) {
        "${row.entry.previousStock} → ${row.entry.newStock}"
    } else if (row.entry.newStock != null) {
        "${row.entry.newStock}"
    } else {
        "-"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = row.medicationName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$reasonText · ${dateFormat.format(Date(row.entry.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = changeText,
                style = MaterialTheme.typography.titleMedium,
                color = tint
            )
        }
    }
}
