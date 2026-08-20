package com.gokcank.curalis.presentation.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Symptom
import com.gokcank.curalis.domain.model.SymptomType
import com.gokcank.curalis.domain.usecase.SymptomUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SymptomListViewModel @Inject constructor(
    private val symptomUseCases: SymptomUseCases
) : ViewModel() {

    private val _symptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val symptoms = _symptoms.asStateFlow()

    private val _selectedType = MutableStateFlow<SymptomType?>(null)
    val selectedType = _selectedType.asStateFlow()

    init {
        loadSymptoms()
    }

    private fun loadSymptoms() {
        viewModelScope.launch {
            if (_selectedType.value == null) {
                symptomUseCases.getSymptoms().collect { _symptoms.value = it }
            } else {
                symptomUseCases.getSymptomsByType(_selectedType.value!!).collect { _symptoms.value = it }
            }
        }
    }

    fun setFilterType(type: SymptomType?) {
        _selectedType.value = type
        loadSymptoms()
    }

    fun deleteSymptom(symptom: Symptom) {
        viewModelScope.launch {
            symptomUseCases.deleteSymptom(symptom)
        }
    }
}
