package com.gokcank.curalis.presentation.vital

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.presentation.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalListScreen(
    viewModel: VitalListViewModel = hiltViewModel(),
    onAddVitalClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToReminderSettings: () -> Unit = {}
) {
    val vitals by viewModel.vitals.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    var vitalToDelete by remember { mutableStateOf<Vital?>(null) }

    vitalToDelete?.let { vital ->
        AlertDialog(
            onDismissRequest = { vitalToDelete = null },
            title = { Text(stringResource(R.string.delete_vital_title)) },
            text = { Text(stringResource(R.string.delete_vital_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVital(vital)
                        vitalToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vitalToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_vitals)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToReminderSettings) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Ölçüm Hatırlatıcıları")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVitalClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_vital))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { viewModel.setFilterType(null) },
                        label = { Text(stringResource(R.string.all)) }
                    )
                }
                items(VitalType.values()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { viewModel.setFilterType(type) },
                        label = { Text(stringResource(type.displayNameRes())) }
                    )
                }
            }

            if (selectedType != null && vitals.size >= 2) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    VitalTrendChart(vitals = vitals, type = selectedType!!)
                }
            }

            if (vitals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedType != null) {
                        // Kayıt var, yalnızca bu filtreye uyan yok.
                        EmptyState(
                            icon = Icons.Default.FilterAlt,
                            title = "Bu türde ölçüm yok",
                            description = "Seçtiğiniz ölçüm türünde kayıt bulunmuyor. Filtreyi kaldırarak tüm ölçümleri görebilirsiniz.",
                            actionLabel = "Filtreyi Kaldır",
                            onAction = { viewModel.setFilterType(null) }
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Default.MonitorHeart,
                            title = stringResource(R.string.no_vitals_found),
                            description = "Tansiyon, kan şekeri veya kilo gibi ölçümlerinizi kaydederek zaman içindeki değişimi takip edebilirsiniz.",
                            actionLabel = stringResource(R.string.add_vital),
                            onAction = onAddVitalClick
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vitals) { vital ->
                        VitalItem(
                            vital = vital,
                            onDelete = { vitalToDelete = vital }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VitalItem(
    vital: Vital,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(vital.timeInMillis))
    
    val valueText = if (vital.value2 != null) {
        "${vital.value1.toInt()}/${vital.value2.toInt()} ${vital.unit}"
    } else {
        "${vital.value1} ${vital.unit}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onDelete
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (vital.type) {
                        VitalType.BLOOD_PRESSURE -> Icons.Default.MonitorHeart
                        VitalType.BLOOD_SUGAR -> Icons.Default.Bloodtype
                        VitalType.HEART_RATE -> Icons.Default.Favorite
                        VitalType.WEIGHT -> Icons.Default.Scale
                        VitalType.TEMPERATURE -> Icons.Default.Thermostat
                        VitalType.OXYGEN_SATURATION -> Icons.Default.Air
                        VitalType.CHOLESTEROL -> Icons.Default.WaterDrop
                        VitalType.A1C -> Icons.Default.Science
                        VitalType.HDL_CHOLESTEROL -> Icons.Default.WaterDrop
                        VitalType.LDL_CHOLESTEROL -> Icons.Default.WaterDrop
                        VitalType.TRIGLYCERIDES -> Icons.Default.WaterDrop
                        VitalType.BODY_FAT -> Icons.Default.Scale
                        VitalType.STEP_COUNT -> Icons.Default.DirectionsWalk
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(vital.type.displayNameRes()),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}
