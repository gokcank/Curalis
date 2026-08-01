package com.gokcank.curalis.data.mapper

import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.domain.model.Medication

fun MedicationEntity.toDomain(): Medication {
    return Medication(
        id = id,
        name = name,
        barcode = barcode,
        activeIngredient = activeIngredient,
        form = form,
        dosage = dosage,
        unit = unit,
        notes = notes
    )
}

fun Medication.toEntity(): MedicationEntity {
    return MedicationEntity(
        id = id,
        name = name,
        barcode = barcode,
        activeIngredient = activeIngredient,
        form = form,
        dosage = dosage,
        unit = unit,
        notes = notes
    )
}
