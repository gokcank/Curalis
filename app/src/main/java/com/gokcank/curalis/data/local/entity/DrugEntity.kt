package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drug_entity")
data class DrugEntity(
    @PrimaryKey
    val gtin: String,
    val drugName: String,
    val activeIngredient: String,
    val manufacturer: String,
    /** Adın aramaya uygun biçimi (bkz. TurkishSearchText); sözlük üretilirken yazılır. */
    val searchName: String
)

/** Yazım önerileri için yalnızca ad sütunları. */
data class DrugNameRow(
    val drugName: String,
    val searchName: String
)
