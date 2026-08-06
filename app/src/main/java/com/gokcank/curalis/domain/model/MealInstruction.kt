package com.gokcank.curalis.domain.model

enum class MealInstruction(val displayNameTr: String, val iconEmoji: String) {
    BEFORE_MEAL("Aç Karnına", "🥣"),
    WITH_MEAL("Yemekle Birlikte", "🍽️"),
    AFTER_MEAL("Tok Karnına", "🍲"),
    DOES_NOT_MATTER("Fark Etmez", "🕒");

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
