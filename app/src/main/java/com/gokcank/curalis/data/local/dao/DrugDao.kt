package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.DrugEntity
import com.gokcank.curalis.data.local.entity.DrugNameRow

@Dao
interface DrugDao {
    /** [normalizedQuery], TurkishSearchText.normalize ile hazırlanmış olmalı. */
    @Query("SELECT * FROM drug_entity WHERE searchName LIKE '%' || :normalizedQuery || '%' LIMIT 50")
    suspend fun searchDrugsByName(normalizedQuery: String): List<DrugEntity>

    @Query("SELECT drugName, searchName FROM drug_entity")
    suspend fun getAllNames(): List<DrugNameRow>

    @Query("SELECT * FROM drug_entity WHERE gtin = :gtin LIMIT 1")
    suspend fun getDrugByGtin(gtin: String): DrugEntity?
}
