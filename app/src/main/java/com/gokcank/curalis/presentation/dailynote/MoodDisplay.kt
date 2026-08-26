package com.gokcank.curalis.presentation.dailynote

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.Mood

fun Mood.emoji(): String = when (this) {
    Mood.VERY_BAD -> "😢"
    Mood.BAD -> "😕"
    Mood.NEUTRAL -> "😐"
    Mood.GOOD -> "🙂"
    Mood.VERY_GOOD -> "😄"
}

@Composable
fun Mood.label(): String = when (this) {
    Mood.VERY_BAD -> stringResource(R.string.mood_very_bad)
    Mood.BAD -> stringResource(R.string.mood_bad)
    Mood.NEUTRAL -> stringResource(R.string.mood_neutral)
    Mood.GOOD -> stringResource(R.string.mood_good)
    Mood.VERY_GOOD -> stringResource(R.string.mood_very_good)
}
