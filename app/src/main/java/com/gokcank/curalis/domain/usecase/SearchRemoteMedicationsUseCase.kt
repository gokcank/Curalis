package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.data.provider.ProviderManager
import com.gokcank.curalis.domain.model.ProviderMedication
import javax.inject.Inject

class SearchRemoteMedicationsUseCase @Inject constructor(
    private val providerManager: ProviderManager
) {
    suspend operator fun invoke(query: String): List<ProviderMedication> {
        return providerManager.searchAllProviders(query)
    }

    /** Arama sonuç vermediğinde "Bunu mu demek istediniz?" için yazımı yakın adlar. */
    suspend fun suggestSpellings(query: String): List<String> = providerManager.suggestSpellings(query)
}
