package com.gokcank.curalis.data.mapper

import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationForm

fun MedicationEntity.toDomain(): Medication {
    val freqType = try {
        FrequencyType.valueOf(frequencyType)
    } catch (e: Exception) {
        FrequencyType.DAILY
    }

    return Medication(
        id = id,
        name = name,
        barcode = barcode,
        activeIngredient = activeIngredient,
        form = form,
        formType = MedicationForm.fromString(formType),
        dosage = dosage,
        unit = unit,
        notes = notes,
        frequencyType = freqType,
        intervalDays = intervalDays,
        activeDays = activeDays,
        restDays = restDays,
        startDate = startDate,
        initialStock = initialStock,
        currentStock = currentStock,
        refillThreshold = refillThreshold,
        isRefillReminderEnabled = isRefillReminderEnabled
    )
}

fun Medication.toEntity(): MedicationEntity {
    return MedicationEntity(
        id = id,
        name = name,
        barcode = barcode,
        activeIngredient = activeIngredient,
        form = form,
        formType = formType.name,
        dosage = dosage,
        unit = unit,
        notes = notes,
        frequencyType = frequencyType.name,
        intervalDays = intervalDays,
        activeDays = activeDays,
        restDays = restDays,
        startDate = startDate,
        initialStock = initialStock,
        currentStock = currentStock,
        refillThreshold = refillThreshold,
        isRefillReminderEnabled = isRefillReminderEnabled
    )
}
