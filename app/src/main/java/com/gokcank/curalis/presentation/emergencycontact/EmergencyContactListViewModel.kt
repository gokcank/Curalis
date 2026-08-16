package com.gokcank.curalis.presentation.emergencycontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.EmergencyContact
import com.gokcank.curalis.domain.usecase.EmergencyContactUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyContactListViewModel @Inject constructor(
    private val emergencyContactUseCases: EmergencyContactUseCases
) : ViewModel() {

    val contacts: StateFlow<List<EmergencyContact>> = emergencyContactUseCases.getEmergencyContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            emergencyContactUseCases.deleteEmergencyContact(contact)
        }
    }
}
