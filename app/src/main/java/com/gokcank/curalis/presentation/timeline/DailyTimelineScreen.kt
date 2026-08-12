package com.gokcank.curalis.presentation.timeline

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.presentation.components.EmptyState
import com.gokcank.curalis.presentation.components.ReminderStateBadge
import com.gokcank.curalis.presentation.components.SkipReasonDialog
import com.gokcank.curalis.presentation.components.icon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTimelineScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyTimelineViewModel = hiltViewModel()
) {
    val groupedTimelineItems by viewModel.groupedTimelineItems.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var pendingSkipReminderId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Günlük Zaman Çizelgesi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Date Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Daha önce month.name kullanılıyordu; bu enum adını ("AUGUST") basıyordu.
                    val monthName = selectedDate.month
                        .getDisplayName(java.time.format.TextStyle.FULL, Locale("tr"))
                        .replaceFirstChar { it.titlecase(Locale("tr")) }
                    Text(
                        text = "${selectedDate.dayOfMonth} $monthName ${selectedDate.year}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Saat dilimlerine göre sıralanmış günlük doz takibi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (groupedTimelineItems.isEmpty() || groupedTimelineItems.values.all { it.isEmpty() }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.EventAvailable,
                        title = "Bugün için doz planlanmamış",
                        description = "Bugüne ait bir ilaç saatiniz yok. İlaçlarınıza saat eklerseniz dozlar burada listelenir."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeSlot.entries.forEach { slot ->
                        val itemsInSlot = groupedTimelineItems[slot] ?: emptyList()
                        if (itemsInSlot.isNotEmpty()) {
                            item {
                                TimeSlotHeader(slot = slot)
                            }
                            items(itemsInSlot) { timelineItem ->
                                TimelineCard(
                                    item = timelineItem,
                                    onTakeClick = { viewModel.acknowledgeDose(timelineItem.reminder.id, ReminderState.TAKEN) },
                                    onSkipClick = { pendingSkipReminderId = timelineItem.reminder.id }
                                )
                            }
                        }
                    }
                }
            }
        }

        pendingSkipReminderId?.let { reminderId ->
            SkipReasonDialog(
                onDismiss = { pendingSkipReminderId = null },
                onConfirm = { reason ->
                    viewModel.acknowledgeDose(reminderId, ReminderState.SKIPPED, reason)
                    pendingSkipReminderId = null
                }
            )
        }
    }
}

@Composable
fun TimeSlotHeader(slot: TimeSlot) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = when (slot) {
                TimeSlot.MORNING -> Icons.Outlined.WbTwilight
                TimeSlot.AFTERNOON -> Icons.Outlined.LightMode
                TimeSlot.EVENING -> Icons.Outlined.WbTwilight
                TimeSlot.NIGHT -> Icons.Outlined.DarkMode
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(slot.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            String.format(Locale.getDefault(), "%02d:00 – %02d:00", slot.startHour, slot.endHour),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimelineCard(
    item: TimelineItem,
    onTakeClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val med = item.medication
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = timeFormat.format(Date(item.reminder.timeInMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = (med?.formType ?: MedicationForm.PILL).icon(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = med?.name ?: "İlaç",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dosageSuffix = med?.dosage?.let { " • $it ${med.unit ?: ""}".trimEnd() } ?: ""
                        Text(
                            text = "Saat $formattedTime$dosageSuffix",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                ReminderStateBadge(state = item.reminder.state)
            }

            if (item.reminder.state == ReminderState.SCHEDULED ||
                item.reminder.state == ReminderState.DELIVERED ||
                item.reminder.state == ReminderState.SNOOZED
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTakeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aldım", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onSkipClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Atla", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
