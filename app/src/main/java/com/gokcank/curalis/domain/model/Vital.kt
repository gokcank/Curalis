package com.gokcank.curalis.domain.model

import java.util.UUID

data class Vital(
    val id: String = UUID.randomUUID().toString(),
    val type: VitalType,
    val value1: Double,
    val value2: Double? = null,
    val unit: String,
    val timeInMillis: Long = System.currentTimeMillis(),
    val notes: String? = null
)
