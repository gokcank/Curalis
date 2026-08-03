package com.gokcank.curalis.presentation.doctor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.usecase.DoctorUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditDoctorViewModel @Inject constructor(
    private val doctorUseCases: DoctorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _doctorName = MutableStateFlow("")
    val doctorName = _doctorName.asStateFlow()

    private val _specialty = MutableStateFlow("")
    val specialty = _specialty.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _hospitalName = MutableStateFlow("")
    val hospitalName = _hospitalName.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentDoctorId: String? = null

    init {
        savedStateHandle.get<String>("doctorId")?.let { doctorId ->
            if (doctorId.isNotBlank()) {
                viewModelScope.launch {
                    doctorUseCases.getDoctorById(doctorId).collect { doctor ->
                        doctor?.let {
                            currentDoctorId = it.id
                            _doctorName.value = it.name
                            _specialty.value = it.specialty ?: ""
                            _phoneNumber.value = it.phoneNumber ?: ""
                            _email.value = it.email ?: ""
                            _hospitalName.value = it.hospitalName ?: ""
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _doctorName.value = name
    }

    fun onSpecialtyChange(specialty: String) {
        _specialty.value = specialty
    }

    fun onPhoneNumberChange(phone: String) {
        _phoneNumber.value = phone
    }

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onHospitalChange(hospital: String) {
        _hospitalName.value = hospital
    }

    fun saveDoctor() {
        viewModelScope.launch {
            if (_doctorName.value.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Doktor adı boş olamaz"))
                return@launch
            }

            val doctorId = currentDoctorId ?: UUID.randomUUID().toString()
            val doctor = Doctor(
                id = doctorId,
                name = _doctorName.value,
                specialty = _specialty.value.takeIf { it.isNotBlank() },
                phoneNumber = _phoneNumber.value.takeIf { it.isNotBlank() },
                email = _email.value.takeIf { it.isNotBlank() },
                hospitalName = _hospitalName.value.takeIf { it.isNotBlank() }
            )

            if (currentDoctorId != null) {
                doctorUseCases.updateDoctor(doctor)
            } else {
                doctorUseCases.addDoctor(doctor)
            }
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveSuccess : UiEvent()
    }
}
