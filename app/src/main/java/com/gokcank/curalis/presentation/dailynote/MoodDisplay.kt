package com.gokcank.curalis.presentation.dailynote

import com.gokcank.curalis.domain.model.Mood

fun Mood.emoji(): String = when (this) {
    Mood.VERY_BAD -> "😢"
    Mood.BAD -> "😕"
    Mood.NEUTRAL -> "😐"
    Mood.GOOD -> "🙂"
    Mood.VERY_GOOD -> "😄"
}

fun Mood.label(): String = when (this) {
    Mood.VERY_BAD -> "Çok Kötü"
    Mood.BAD -> "Kötü"
    Mood.NEUTRAL -> "Normal"
    Mood.GOOD -> "İyi"
    Mood.VERY_GOOD -> "Çok İyi"
}
