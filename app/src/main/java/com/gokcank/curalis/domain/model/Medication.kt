package com.gokcank.curalis.domain.model

import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String? = null,
    val activeIngredient: String? = null,
    val form: String? = null,
    val dosage: String? = null,
    val unit: String? = null,
    val notes: String? = null
)
