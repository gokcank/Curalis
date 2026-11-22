package com.gokcank.curalis.presentation.medication.list

import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.Medication

/**
 * İlaç listesinin paylaşılacak düz metni: her etkin ilaç için ad (ve dozaj), hatırlatma
 * saatleri (saat başına doz yazılıysa parantez içinde), sıklık ve yemek talimatı. Metinler
 * çağıran taraftan dil kaynaklarıyla gelir; burada yalnızca biçim kurulur.
 */
object MedicationShareText {

    data class Labels(
        /** Başlık satırı, tarih dahil (ör. "İlaçlarım — 23.11.2026"). */
        val title: String,
        val times: String,
        val frequency: String,
        val meal: String
    )

    fun build(
        medications: List<Medication>,
        labels: Labels,
        frequencyOf: (Medication) -> String,
        mealOf: (MealInstruction) -> String
    ): String = buildString {
        append(labels.title)
        medications.forEach { med ->
            append("\n\n• ")
            append(med.name)
            val strength = listOfNotNull(med.dosage?.trim(), med.unit?.trim())
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            if (med.dosage?.isNotBlank() == true) append(" · ").append(strength)

            val times = med.times.sortedWith(compareBy({ it.hour }, { it.minute })).joinToString(", ") { time ->
                val clock = String.format(java.util.Locale.ROOT, "%02d:%02d", time.hour, time.minute)
                time.dose?.trim()?.takeIf { it.isNotEmpty() }?.let { "$clock ($it)" } ?: clock
            }
            if (times.isNotEmpty()) append("\n  ").append(labels.times).append(": ").append(times)
            append("\n  ").append(labels.frequency).append(": ").append(frequencyOf(med))
            if (med.mealInstruction != MealInstruction.DOES_NOT_MATTER) {
                append("\n  ").append(labels.meal).append(": ").append(mealOf(med.mealInstruction))
            }
        }
    }

    /** Paylaşılan ilaçlar: arşivlenmiş ve askıdaki ilaçlar girmez; ilk saate, sonra ada göre sıralı. */
    fun shareable(medications: List<Medication>): List<Medication> =
        medications
            .filter { !it.isArchived && !it.isSuspended }
            .sortedWith(
                compareBy<Medication>(
                    { it.times.minOfOrNull { t -> t.hour * 60 + t.minute } ?: Int.MAX_VALUE },
                    { it.name.lowercase() }
                )
            )
}
