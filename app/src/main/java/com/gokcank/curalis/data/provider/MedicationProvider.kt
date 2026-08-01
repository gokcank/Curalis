package com.gokcank.curalis.data.provider

import com.gokcank.curalis.domain.model.ProviderMedication

interface MedicationProvider {
    val providerName: String
    suspend fun searchMedications(query: String): List<ProviderMedication>
}
