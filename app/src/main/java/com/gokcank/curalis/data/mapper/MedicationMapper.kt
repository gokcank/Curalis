package com.gokcank.curalis.data.mapper

import com.gokcank.curalis.data.local.entity.MedicationEntity
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.MedicationTime

fun MedicationEntity.toDomain(times: List<MedicationTime> = emptyList(), specificDays: List<Int> = emptyList()): Medication {
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
        colorHex = colorHex,
        iconShape = iconShape,
        dosage = dosage,
        unit = unit,
        notes = notes,
        mealInstruction = MealInstruction.fromString(mealInstruction),
        expiryDate = expiryDate,
        frequencyType = freqType,
        intervalDays = intervalDays,
        activeDays = activeDays,
        restDays = restDays,
        hasPlaceboDays = hasPlaceboDays,
        startDate = startDate,
        initialStock = initialStock,
        currentStock = currentStock,
        refillThreshold = refillThreshold,
        isRefillReminderEnabled = isRefillReminderEnabled,
        isVerifiedSource = isVerifiedSource,
        isArchived = isArchived,
        isSuspended = isSuspended,
        treatmentDurationDays = treatmentDurationDays,
        rxNumber = rxNumber,
        photoPath = photoPath,
        doctorId = doctorId,
        times = times,
        specificDays = specificDays
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
        colorHex = colorHex,
        iconShape = iconShape,
        dosage = dosage,
        unit = unit,
        notes = notes,
        mealInstruction = mealInstruction.name,
        expiryDate = expiryDate,
        frequencyType = frequencyType.name,
        intervalDays = intervalDays,
        activeDays = activeDays,
        restDays = restDays,
        hasPlaceboDays = hasPlaceboDays,
        startDate = startDate,
        initialStock = initialStock,
        currentStock = currentStock,
        refillThreshold = refillThreshold,
        isRefillReminderEnabled = isRefillReminderEnabled,
        isVerifiedSource = isVerifiedSource,
        isArchived = isArchived,
        isSuspended = isSuspended,
        treatmentDurationDays = treatmentDurationDays,
        rxNumber = rxNumber,
        photoPath = photoPath,
        doctorId = doctorId
    )
}

fun MedicationTimeEntity.toDomain(): MedicationTime {
    return MedicationTime(
        id = id,
        hour = hour,
        minute = minute,
        dose = dose
    )
}

fun MedicationTime.toEntity(medicationId: String): MedicationTimeEntity {
    return MedicationTimeEntity(
        id = id,
        medicationId = medicationId,
        hour = hour,
        minute = minute,
        dose = dose
    )
}
