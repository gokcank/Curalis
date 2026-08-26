package com.gokcank.curalis.presentation.symptom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Sick
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.SymptomType

@Composable
fun SymptomType.displayName(): String = when (this) {
    SymptomType.PAIN -> stringResource(R.string.symptom_type_pain)
    SymptomType.NAUSEA -> stringResource(R.string.symptom_type_nausea)
    SymptomType.FATIGUE -> stringResource(R.string.symptom_type_fatigue)
}

fun SymptomType.icon(): ImageVector = when (this) {
    SymptomType.PAIN -> Icons.Default.Healing
    SymptomType.NAUSEA -> Icons.Default.Sick
    SymptomType.FATIGUE -> Icons.Default.Bolt
}

/** 0-10 öznel şiddet skalasını kısa bir niteleyiciye çevirir. */
@Composable
fun severityLabel(severity: Int): String = when (severity) {
    0 -> stringResource(R.string.severity_none)
    in 1..3 -> stringResource(R.string.severity_mild)
    in 4..6 -> stringResource(R.string.severity_moderate)
    in 7..9 -> stringResource(R.string.severity_severe)
    else -> stringResource(R.string.severity_unbearable)
}
