package com.gokcank.curalis.presentation.doctor

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
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.gokcank.curalis.core.theme.SectionAccentDoctors
import com.gokcank.curalis.core.theme.rememberSectionThemeMode
import com.gokcank.curalis.core.theme.sectionBackgroundModifier
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.presentation.components.EmptyState

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
fun DoctorListScreen(
    viewModel: DoctorListViewModel = hiltViewModel(),
    onAddDoctorClick: () -> Unit,
    onDoctorClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit = {}
) {
    val doctors by viewModel.doctors.collectAsState()
    var doctorToDelete by remember { mutableStateOf<Doctor?>(null) }

    doctorToDelete?.let { doctor ->
        AlertDialog(
            onDismissRequest = { doctorToDelete = null },
            title = { Text(stringResource(R.string.delete_doctor_title)) },
            text = { Text(stringResource(R.string.delete_doctor_message, doctor.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDoctor(doctor)
                        doctorToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { doctorToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val themeMode by rememberSectionThemeMode()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_doctors)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEmergencyContacts) {
                        Icon(Icons.Default.ContactEmergency, contentDescription = stringResource(R.string.emergency_contacts_content_desc))
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDoctorClick,
                containerColor = SectionAccentDoctors,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_doctor))
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (doctors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(sectionBackgroundModifier(themeMode))
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.no_doctors_found),
                    description = stringResource(R.string.no_doctors_empty_state_desc),
                    actionLabel = stringResource(R.string.add_doctor),
                    onAction = onAddDoctorClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(sectionBackgroundModifier(themeMode))
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(doctors) { doctor ->
                    DoctorItem(
                        doctor = doctor,
                        onClick = { onDoctorClick(doctor.id) },
                        onDelete = { doctorToDelete = doctor }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoctorItem(
    doctor: Doctor,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
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
                color = SectionAccentDoctors,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!doctor.specialty.isNullOrBlank()) {
                    Text(
                        text = doctor.specialty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
