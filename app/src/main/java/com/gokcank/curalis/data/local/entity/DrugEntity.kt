package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drug_entity")
data class DrugEntity(
    @PrimaryKey
    val gtin: String,
    val drugName: String,
    val activeIngredient: String,
    val manufacturer: String
)
