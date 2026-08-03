package com.gokcank.curalis.data.provider

import com.gokcank.curalis.data.provider.local.LocalMedicationProvider
import com.gokcank.curalis.domain.model.ProviderMedication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderManager @Inject constructor(
    private val localMedicationProvider: LocalMedicationProvider
) {
    private val providers: List<MedicationProvider> = listOf(localMedicationProvider)

    suspend fun searchAllProviders(query: String): List<ProviderMedication> {
        if (query.length < 2) return emptyList()

        for (provider in providers) {
            val results = provider.searchMedications(query)
            if (results.isNotEmpty()) {
                return results
            }
        }
        return emptyList()
    }
}
