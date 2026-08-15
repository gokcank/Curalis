package com.gokcank.curalis.domain.model

enum class MedicationForm(
    val displayNameTr: String,
    val displayNameEn: String
) {
    PILL("Hap / Tablet", "Pill / Tablet"),
    CAPSULE("Kapsül", "Capsule"),
    SYRUP("Şurup / Likit", "Syrup / Liquid"),
    INJECTION("İğne / Enjeksiyon", "Injection"),
    INJECTION_PEN("Enjeksiyon Kalemi", "Injection Pen"),
    OINTMENT("Merhem / Krem", "Ointment / Cream"),
    GEL("Jel", "Gel"),
    DROPS("Damla", "Drops"),
    INHALER("İnhaler / Fısfıs", "Inhaler"),
    SPRAY("Sprey", "Spray"),
    SUPPOSITORY("Fitil", "Suppository"),
    PATCH("Bant / Yakı", "Patch"),
    LOZENGE("Pastil / Emilebilir Tablet", "Lozenge"),
    POWDER("Toz / Saşe", "Powder / Sachet"),
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
