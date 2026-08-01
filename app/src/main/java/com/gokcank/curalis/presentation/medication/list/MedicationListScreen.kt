package com.gokcank.curalis.presentation.medication.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.domain.model.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onNavigateToAddEdit: (String?) -> Unit,
    viewModel: MedicationListViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Medications") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddEdit(null) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, barcode...") },
                singleLine = true
            )
            
            if (medications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No medications found. Tap + to add one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                ) {
                    items(medications) { medication ->
                        MedicationItem(
                            medication = medication,
                            onClick = { onNavigateToAddEdit(medication.id) },
                            onDelete = { viewModel.deleteMedication(medication) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationItem(
    medication: Medication,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medication.name, style = MaterialTheme.typography.titleMedium)
                if (!medication.activeIngredient.isNullOrBlank()) {
                    Text(text = medication.activeIngredient, style = MaterialTheme.typography.bodySmall)
                }
                val dosageStr = listOfNotNull(medication.dosage, medication.unit).joinToString(" ")
                if (dosageStr.isNotBlank()) {
                    Text(text = dosageStr, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
