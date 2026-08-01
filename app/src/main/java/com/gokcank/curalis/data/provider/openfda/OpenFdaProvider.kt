package com.gokcank.curalis.data.provider.openfda

import com.gokcank.curalis.data.provider.MedicationProvider
import com.gokcank.curalis.domain.model.ProviderMedication
import javax.inject.Inject

class OpenFdaProvider @Inject constructor(
    private val api: OpenFdaApi
) : MedicationProvider {

    override val providerName: String = "OpenFDA"

    override suspend fun searchMedications(query: String): List<ProviderMedication> {
        return try {
            val searchQuery = "brand_name:$query* OR generic_name:$query*"
            val response = api.searchMedications(searchQuery)
            
            response.results?.map { result ->
                val activeIngredientName = result.activeIngredients?.firstOrNull()?.name
                    ?: result.genericName
                val activeIngredientStrength = result.activeIngredients?.firstOrNull()?.strength

                ProviderMedication(
                    name = result.brandName ?: result.genericName ?: query,
                    activeIngredient = activeIngredientName,
                    form = result.dosageForm,
                    dosage = activeIngredientStrength
                )
            }?.distinctBy { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
