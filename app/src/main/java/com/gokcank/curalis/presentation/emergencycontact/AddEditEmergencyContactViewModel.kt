package com.gokcank.curalis.presentation.emergencycontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.EmergencyContact
import com.gokcank.curalis.domain.usecase.EmergencyContactUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditEmergencyContactViewModel @Inject constructor(
    private val emergencyContactUseCases: EmergencyContactUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _relationship = MutableStateFlow("")
    val relationship = _relationship.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentContactId: String? = null
    val isEditMode: Boolean
        get() = currentContactId != null

    init {
        savedStateHandle.get<String>("contactId")?.let { contactId ->
            if (contactId.isNotBlank()) {
                viewModelScope.launch {
                    emergencyContactUseCases.getEmergencyContactById(contactId).collect { contact ->
                        contact?.let {
                            currentContactId = it.id
                            _name.value = it.name
                            _relationship.value = it.relationship ?: ""
                            _phoneNumber.value = it.phoneNumber ?: ""
                            _notes.value = it.notes ?: ""
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) { _name.value = value }
    fun onRelationshipChange(value: String) { _relationship.value = value }
    fun onPhoneNumberChange(value: String) { _phoneNumber.value = value }
    fun onNotesChange(value: String) { _notes.value = value }

    fun saveContact() {
        viewModelScope.launch {
            if (_name.value.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Kişi adı boş olamaz"))
                return@launch
            }

            val contactId = currentContactId ?: UUID.randomUUID().toString()
            val contact = EmergencyContact(
                id = contactId,
                name = _name.value,
                relationship = _relationship.value.takeIf { it.isNotBlank() },
                phoneNumber = _phoneNumber.value.takeIf { it.isNotBlank() },
                notes = _notes.value.takeIf { it.isNotBlank() }
            )

            if (currentContactId != null) {
                emergencyContactUseCases.updateEmergencyContact(contact)
            } else {
                emergencyContactUseCases.addEmergencyContact(contact)
            }
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveSuccess : UiEvent()
    }
}
