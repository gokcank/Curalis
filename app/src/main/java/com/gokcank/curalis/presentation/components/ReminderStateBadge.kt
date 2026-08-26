package com.gokcank.curalis.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.domain.model.ReminderState

/**
 * Bir hatırlatıcının durumunu gösteren rozet.
 *
 * Daha önce bu eşleme takvim ve zaman çizelgesi ekranlarında ayrı ayrı, sabit açık-tema
 * renkleriyle kopyalanmıştı; koyu temada beyaz lekeler oluşuyordu. Artık tek yerde ve
 * tema renkleriyle tanımlı.
 *
 * design-system.md: "Kaçırıldı" bir sistem hatası değil, dikkat gerektiren bir durumdur —
 * bu yüzden Error değil Warning kullanır. Error yalnızca gerçek hatalar/tehlikeli
 * eylemler içindir.
 */
private data class ReminderStateStyle(
    val label: String,
    val icon: ImageVector,
    val container: Color,
    val content: Color
)

@Composable
private fun styleFor(state: ReminderState): ReminderStateStyle {
    val semantic = LocalCuralisColors.current
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        ReminderState.TAKEN -> ReminderStateStyle(
            stringResource(R.string.reminder_state_taken), Icons.Filled.CheckCircle, semantic.successContainer, semantic.onSuccessContainer
        )
        ReminderState.SKIPPED -> ReminderStateStyle(
            stringResource(R.string.reminder_state_skipped), Icons.Outlined.RemoveCircle, scheme.surfaceVariant, scheme.onSurfaceVariant
        )
        ReminderState.MISSED -> ReminderStateStyle(
            stringResource(R.string.reminder_state_missed), Icons.Filled.Warning, semantic.warningContainer, semantic.onWarningContainer
        )
        ReminderState.SNOOZED -> ReminderStateStyle(
            stringResource(R.string.reminder_state_snoozed), Icons.Filled.Snooze, semantic.infoContainer, semantic.onInfoContainer
        )
        ReminderState.CANCELLED -> ReminderStateStyle(
            stringResource(R.string.reminder_state_cancelled), Icons.Filled.Block, scheme.surfaceVariant, scheme.onSurfaceVariant
        )
        ReminderState.SCHEDULED -> ReminderStateStyle(
            stringResource(R.string.reminder_state_scheduled), Icons.Filled.Schedule, scheme.surfaceVariant, scheme.onSurfaceVariant
        )
        ReminderState.DELIVERED -> ReminderStateStyle(
            stringResource(R.string.reminder_state_delivered), Icons.Filled.Schedule, semantic.infoContainer, semantic.onInfoContainer
        )
    }
}

@Composable
fun ReminderStateBadge(
    state: ReminderState,
    modifier: Modifier = Modifier
) {
    val style = styleFor(state)
    InfoChip(
        icon = style.icon,
        text = style.label,
        containerColor = style.container,
        contentColor = style.content,
        modifier = modifier
    )
}
