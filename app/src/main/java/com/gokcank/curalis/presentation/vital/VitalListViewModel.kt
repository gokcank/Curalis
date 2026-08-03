package com.gokcank.curalis.presentation.vital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.usecase.VitalUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VitalListViewModel @Inject constructor(
    private val vitalUseCases: VitalUseCases
) : ViewModel() {

    private val _vitals = MutableStateFlow<List<Vital>>(emptyList())
    val vitals = _vitals.asStateFlow()

    private val _selectedType = MutableStateFlow<VitalType?>(null)
    val selectedType = _selectedType.asStateFlow()

    init {
        loadVitals()
    }

    private fun loadVitals() {
        viewModelScope.launch {
            if (_selectedType.value == null) {
                vitalUseCases.getVitals().collect { vitalList ->
                    _vitals.value = vitalList
                }
            } else {
                vitalUseCases.getVitalsByType(_selectedType.value!!).collect { vitalList ->
                    _vitals.value = vitalList
                }
            }
        }
    }

    fun setFilterType(type: VitalType?) {
        _selectedType.value = type
        loadVitals()
    }

    fun deleteVital(vital: Vital) {
        viewModelScope.launch {
            vitalUseCases.deleteVital(vital)
        }
    }
}
