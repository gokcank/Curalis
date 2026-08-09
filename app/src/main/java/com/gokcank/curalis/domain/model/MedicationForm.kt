package com.gokcank.curalis.domain.model

enum class MedicationForm(
    val displayNameTr: String,
    val displayNameEn: String
) {
    PILL("Hap / Tablet", "Pill / Tablet"),
    CAPSULE("Kapsül", "Capsule"),
    SYRUP("Şurup / Likit", "Syrup / Liquid"),
    INJECTION("İğne / Enjeksiyon", "Injection"),
    OINTMENT("Merhem / Krem", "Ointment / Cream"),
    DROPS("Damla", "Drops"),
    INHALER("İnhaler / Fısfıs", "Inhaler"),
    PATCH("Bant / Yakı", "Patch"),
    OTHER("Diğer", "Other");

    companion object {
        fun fromString(value: String?): MedicationForm {
            if (value.isNullOrBlank()) return PILL
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                entries.find { it.displayNameTr.equals(value, ignoreCase = true) || it.displayNameEn.equals(value, ignoreCase = true) } ?: PILL
            }
        }
    }
}
