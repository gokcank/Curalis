package com.gokcank.curalis.data.provider

import com.gokcank.curalis.domain.model.ProviderMedication

interface MedicationProvider {
    val providerName: String
    suspend fun searchMedications(query: String): List<ProviderMedication>
    suspend fun findByBarcode(gtin: String): ProviderMedication?

    /** Arama sonuç vermediğinde önerilecek, yazımı yakın ilaç adları. */
    suspend fun suggestSpellings(query: String, limit: Int): List<String> = emptyList()
}
