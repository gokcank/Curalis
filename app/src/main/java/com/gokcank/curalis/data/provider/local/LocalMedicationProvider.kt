package com.gokcank.curalis.data.provider.local

import com.gokcank.curalis.data.local.dao.DrugDao
import com.gokcank.curalis.data.provider.MedicationProvider
import com.gokcank.curalis.domain.model.ProviderMedication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMedicationProvider @Inject constructor(
    private val drugDao: DrugDao
) : MedicationProvider {
    
    override val providerName: String = "TİTCK Lokal Veritabanı"

    override suspend fun searchMedications(query: String): List<ProviderMedication> {
        return drugDao.searchDrugsByName(query).map { entity ->
            ProviderMedication(
                name = entity.drugName,
                activeIngredient = entity.activeIngredient,
                form = "", // Forms are embedded in name in TITCK data mostly
                dosage = ""
            )
        }
    }
}
