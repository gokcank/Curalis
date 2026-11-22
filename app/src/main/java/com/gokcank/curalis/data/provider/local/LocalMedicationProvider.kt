package com.gokcank.curalis.data.provider.local

import com.gokcank.curalis.core.utils.TurkishSearchText
import com.gokcank.curalis.data.local.dao.DrugDao
import com.gokcank.curalis.data.local.entity.DrugNameRow
import com.gokcank.curalis.data.local.entity.DrugEntity
import com.gokcank.curalis.data.provider.MedicationProvider
import com.gokcank.curalis.domain.model.ProviderMedication
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMedicationProvider @Inject constructor(
    private val drugDao: DrugDao
) : MedicationProvider {
    
    override val providerName: String = "TİTCK Lokal Veritabanı"

    // SQLite'ın LIKE'ı yalnızca ASCII harflerde büyük/küçük ayrımı yapmaz ("ş" ile "Ş" farklı
    // sonuç veriyordu); arama, sözlükteki searchName sütununda aynı kuralla hazırlanmış
    // sorguyla yapılır.
    override suspend fun searchMedications(query: String): List<ProviderMedication> {
        val normalized = TurkishSearchText.normalize(query)
        if (normalized.isEmpty()) return emptyList()
        return drugDao.searchDrugsByName(normalized).map { it.toProviderMedication() }
    }

    private val namesLock = Mutex()
    private var names: List<DrugNameRow>? = null

    /** Ad listesi ilk öneride bir kez okunur ve bellekte tutulur (sözlük salt okunur). */
    override suspend fun suggestSpellings(query: String, limit: Int): List<String> {
        val all = namesLock.withLock { names ?: drugDao.getAllNames().also { names = it } }
        return MedicationSpelling.suggest(query, all, limit)
    }

    override suspend fun findByBarcode(gtin: String): ProviderMedication? {
        return drugDao.getDrugByGtin(gtin)?.toProviderMedication()
    }

    private fun DrugEntity.toProviderMedication() = ProviderMedication(
        name = drugName,
        activeIngredient = activeIngredient,
        form = "", // Forms are embedded in name in TITCK data mostly
        dosage = ""
    )
}
