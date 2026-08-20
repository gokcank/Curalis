package com.gokcank.curalis.presentation.symptom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Sick
import androidx.compose.ui.graphics.vector.ImageVector
import com.gokcank.curalis.domain.model.SymptomType

fun SymptomType.displayName(): String = when (this) {
    SymptomType.PAIN -> "Ağrı"
    SymptomType.NAUSEA -> "Bulantı"
    SymptomType.FATIGUE -> "Yorgunluk"
}

fun SymptomType.icon(): ImageVector = when (this) {
    SymptomType.PAIN -> Icons.Default.Healing
    SymptomType.NAUSEA -> Icons.Default.Sick
    SymptomType.FATIGUE -> Icons.Default.Bolt
}

/** 0-10 öznel şiddet skalasını kısa bir Türkçe niteleyiciye çevirir. */
fun severityLabel(severity: Int): String = when (severity) {
    0 -> "Yok"
    in 1..3 -> "Hafif"
    in 4..6 -> "Orta"
    in 7..9 -> "Şiddetli"
    else -> "Dayanılmaz"
}
