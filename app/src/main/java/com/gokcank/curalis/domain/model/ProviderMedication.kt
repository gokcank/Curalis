package com.gokcank.curalis.domain.model

data class ProviderMedication(
    val name: String,
    val activeIngredient: String?,
    val form: String?,
    val dosage: String?
)
