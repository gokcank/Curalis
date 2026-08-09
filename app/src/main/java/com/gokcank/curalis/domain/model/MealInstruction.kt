package com.gokcank.curalis.domain.model

enum class MealInstruction(val displayNameTr: String, val displayNameEn: String) {
    BEFORE_MEAL("Aç Karnına", "On Empty Stomach"),
    WITH_MEAL("Yemekle Birlikte", "With Food"),
    AFTER_MEAL("Tok Karnına", "After Food"),
    DOES_NOT_MATTER("Fark Etmez", "Does Not Matter");

    companion object {
        fun fromString(value: String?): MealInstruction {
            if (value == null) return DOES_NOT_MATTER
            return try {
                valueOf(value)
            } catch (e: Exception) {
                DOES_NOT_MATTER
            }
        }
    }
}
