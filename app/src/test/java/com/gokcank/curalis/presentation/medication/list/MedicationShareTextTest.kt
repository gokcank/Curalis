package com.gokcank.curalis.presentation.medication.list

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationShareTextTest {

    private val labels = MedicationShareText.Labels(title = "İlaçlarım — 23 Kas 2026", times = "Saatler", frequency = "Sıklık", meal = "Yemek")
    private val frequencyOf: (Medication) -> String = { if (it.frequencyType == FrequencyType.AS_NEEDED) "İhtiyaç Halinde" else "Her Gün" }
    private val mealOf: (MealInstruction) -> String = { it.displayNameTr }

    @Test
    fun `active medications are listed in time order, archived and suspended are left out`() {
        val meds = MedicationShareText.shareable(
            listOf(
                Medication(id = "b", name = "Akşam", times = listOf(MedicationTime(hour = 20, minute = 0))),
                Medication(id = "a", name = "Sabah", times = listOf(MedicationTime(hour = 8, minute = 0))),
                Medication(id = "c", name = "Arşiv", isArchived = true, times = listOf(MedicationTime(hour = 7, minute = 0))),
                Medication(id = "d", name = "Askıda", isSuspended = true, times = listOf(MedicationTime(hour = 7, minute = 0))),
                Medication(id = "e", name = "Gerektiğinde", frequencyType = FrequencyType.AS_NEEDED)
            )
        )
        assertEquals(listOf("Sabah", "Akşam", "Gerektiğinde"), meds.map { it.name })
    }

    @Test
    fun `each medication shows name, strength, times with per-time dose, frequency and meal`() {
        val text = MedicationShareText.build(
            listOf(
                Medication(
                    id = "a",
                    name = "Aspirin",
                    dosage = "100",
                    unit = "mg",
                    mealInstruction = MealInstruction.AFTER_MEAL,
                    times = listOf(MedicationTime(hour = 20, minute = 0), MedicationTime(hour = 8, minute = 5, dose = "2"))
                ),
                Medication(id = "b", name = "Parol", frequencyType = FrequencyType.AS_NEEDED)
            ),
            labels, frequencyOf, mealOf
        )
        val expected = """
            İlaçlarım — 23 Kas 2026

            • Aspirin · 100 mg
              Saatler: 08:05 (2), 20:00
              Sıklık: Her Gün
              Yemek: Tok Karnına

            • Parol
              Sıklık: İhtiyaç Halinde
        """.trimIndent()
        assertEquals(expected, text)
    }
}
