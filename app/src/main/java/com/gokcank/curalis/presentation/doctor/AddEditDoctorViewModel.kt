package com.gokcank.curalis.presentation.doctor

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.Appointment
import com.gokcank.curalis.domain.model.Doctor
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.usecase.DoctorUseCases
import com.gokcank.curalis.domain.usecase.GetAppointmentsUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditDoctorViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val doctorUseCases: DoctorUseCases,
    getMedicationsUseCase: GetMedicationsUseCase,
    getAppointmentsUseCase: GetAppointmentsUseCase,
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

    // savedStateHandle'daki ham "doctorId" argümanı, ekleme akışında navigasyonun
    // {doctorId} yer tutucusunu literal olarak geçirmesi yüzünden boş olmayan ama
    // geçersiz bir string olabilir (bkz. NavGraph — onAddDoctorClick). Bu yüzden
    // isEditMode, ham argümana değil, veritabanında GERÇEKTEN bulunan bir doktora göre
    // belirlenir; aksi halde yeni ilaç eklerken de "Randevular" sekmesi yanlışlıkla görünürdü.
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    /** Bu doktora bağlı (reçete eden doktoru bu olarak seçilmiş) ilaçlar. Yeni bir doktor
     *  eklenirken (henüz kimliği yokken) her zaman boştur. */
    val linkedMedications: StateFlow<List<Medication>> = savedStateHandle
        .get<String>("doctorId")
        ?.takeIf { it.isNotBlank() }
        ?.let { doctorId ->
            getMedicationsUseCase().map { medications ->
                medications.filter { it.doctorId == doctorId && !it.isArchived }
            }
        }
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    /** Bu doktora atanmış randevular — "Randevular" sekmesinde gösterilir. */
    val linkedAppointments: StateFlow<List<Appointment>> = savedStateHandle
        .get<String>("doctorId")
        ?.takeIf { it.isNotBlank() }
        ?.let { doctorId ->
            getAppointmentsUseCase().map { appointments ->
                appointments.filter { it.doctorId == doctorId }.sortedBy { it.timeInMillis }
            }
        }
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    init {
        savedStateHandle.get<String>("doctorId")?.let { doctorId ->
            if (doctorId.isNotBlank()) {
                viewModelScope.launch {
                    doctorUseCases.getDoctorById(doctorId).collect { doctor ->
                        doctor?.let {
                            currentDoctorId = it.id
                            _isEditMode.value = true
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
                _eventFlow.emit(UiEvent.ShowSnackbar(appContext.getString(R.string.doctor_name_empty_error)))
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
