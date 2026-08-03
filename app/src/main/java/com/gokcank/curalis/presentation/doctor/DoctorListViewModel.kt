package com.gokcank.curalis.presentation.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.usecase.DoctorUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorListViewModel @Inject constructor(
    private val doctorUseCases: DoctorUseCases
) : ViewModel() {

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors = _doctors.asStateFlow()

    init {
        viewModelScope.launch {
            doctorUseCases.getDoctors().collect { doctorList ->
                _doctors.value = doctorList
            }
        }
    }

    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch {
            doctorUseCases.deleteDoctor(doctor)
        }
    }
}
