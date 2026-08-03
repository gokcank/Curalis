package com.gokcank.curalis.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.gokcank.curalis.data.local.entity.DrugEntity

@Dao
interface DrugDao {
    @Query("SELECT * FROM drug_entity WHERE drugName LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchDrugsByName(query: String): List<DrugEntity>

    @Query("SELECT * FROM drug_entity WHERE gtin = :gtin LIMIT 1")
    suspend fun getDrugByGtin(gtin: String): DrugEntity?
}
