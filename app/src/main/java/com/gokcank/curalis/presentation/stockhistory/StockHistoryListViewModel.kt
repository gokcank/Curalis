package com.gokcank.curalis.presentation.stockhistory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StockHistoryRow(
    val entry: StockHistoryEntry,
    val medicationName: String
)

@HiltViewModel
class StockHistoryListViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    stockHistoryRepository: StockHistoryRepository,
    getMedicationsUseCase: GetMedicationsUseCase
) : ViewModel() {

    private val _selectedMedicationId = MutableStateFlow<String?>(null)
    val selectedMedicationId: StateFlow<String?> = _selectedMedicationId

    val medicationNames: StateFlow<List<Pair<String, String>>> = getMedicationsUseCase()
        .map { medications -> medications.map { it.id to it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allRows: StateFlow<List<StockHistoryRow>> = combine(
        stockHistoryRepository.getAllHistory(),
        getMedicationsUseCase()
    ) { history, medications ->
        val nameById = medications.associate { it.id to it.name }
        history.map { entry ->
            StockHistoryRow(entry = entry, medicationName = nameById[entry.medicationId] ?: appContext.getString(R.string.deleted_medication_label))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rows: StateFlow<List<StockHistoryRow>> = combine(allRows, _selectedMedicationId) { rows, medicationId ->
        if (medicationId == null) rows else rows.filter { it.entry.medicationId == medicationId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onMedicationFilterSelected(medicationId: String?) {
        _selectedMedicationId.value = medicationId
    }
}
