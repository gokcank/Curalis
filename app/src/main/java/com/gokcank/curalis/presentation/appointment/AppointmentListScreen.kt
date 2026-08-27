package com.gokcank.curalis.presentation.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.SectionAccentAppointments
import com.gokcank.curalis.core.theme.rememberSectionThemeMode
import com.gokcank.curalis.core.theme.sectionBackgroundModifier
import com.gokcank.curalis.domain.model.Appointment
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
fun AppointmentListScreen(
    viewModel: AppointmentListViewModel = hiltViewModel(),
    onAddAppointmentClick: () -> Unit,
    onAppointmentClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsState()
    val completedAppointments by viewModel.completedAppointments.collectAsState()
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf(stringResource(R.string.tab_upcoming), stringResource(R.string.tab_completed))
    val visibleAppointments = if (selectedTab == 0) upcomingAppointments else completedAppointments

    appointmentToDelete?.let { appt ->
        AlertDialog(
            onDismissRequest = { appointmentToDelete = null },
            title = { Text(stringResource(R.string.delete_appointment_title)) },
            text = { Text(stringResource(R.string.delete_appointment_message, appt.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAppointment(appt)
                        appointmentToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val themeMode by rememberSectionThemeMode()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_appointments)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointmentClick,
                containerColor = SectionAccentAppointments,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_appointment))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        val hasAnyAppointments = upcomingAppointments.isNotEmpty() || completedAppointments.isNotEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(sectionBackgroundModifier(themeMode))
                .padding(padding)
        ) {
            if (hasAnyAppointments) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            if (!hasAnyAppointments) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        title = stringResource(R.string.no_appointments_found),
                        description = stringResource(R.string.no_appointments_empty_state_desc),
                        actionLabel = stringResource(R.string.add_appointment),
                        onAction = onAddAppointmentClick
                    )
                }
            } else if (visibleAppointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        title = if (selectedTab == 0) stringResource(R.string.no_upcoming_appointments_title) else stringResource(R.string.no_completed_appointments_title),
                        description = if (selectedTab == 0) {
                            stringResource(R.string.no_pending_appointments_desc)
                        } else {
                            stringResource(R.string.no_past_appointments_desc)
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleAppointments) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            onClick = { onAppointmentClick(appointment.id) },
                            onDelete = { appointmentToDelete = appointment }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppointmentItem(
    appointment: Appointment,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(appointment.timeInMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = SectionAccentAppointments,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = dateString, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                if (!appointment.location.isNullOrBlank()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (appointment.isVisited) {
                    Text(
                        text = stringResource(R.string.visited),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}
