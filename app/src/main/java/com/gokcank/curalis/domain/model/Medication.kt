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
    val notes: String? = null,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val intervalDays: Int? = null,
    val startDate: Long = System.currentTimeMillis(),
    val specificDays: List<Int> = emptyList(), // 1=Mon, 7=Sun
    val times: List<MedicationTime> = emptyList()
)

data class MedicationTime(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val dose: String? = null
)
