package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.data.provider.ProviderManager
import com.gokcank.curalis.domain.model.ProviderMedication
import javax.inject.Inject

class FindMedicationByBarcodeUseCase @Inject constructor(
    private val providerManager: ProviderManager
) {
    suspend operator fun invoke(gtin: String): ProviderMedication? {
        return providerManager.findByBarcode(gtin)
    }
}
