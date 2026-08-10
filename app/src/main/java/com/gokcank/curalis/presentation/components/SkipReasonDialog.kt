package com.gokcank.curalis.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.SkipReason
import java.util.Locale

/**
 * "Atla" işleminden önce yalnızca uygulama içi ekranlarda (Timeline, tam ekran alarm)
 * gösterilen sebep seçim diyaloğu. Bildirimdeki tek dokunuşluk "Atla" hızını korumak için
 * bu diyalog bildirim akışında gösterilmez.
 */
@Composable
fun SkipReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (SkipReason) -> Unit
) {
    var selectedReason by remember { mutableStateOf(SkipReason.FORGOT) }
    val isEnglish = LocalConfiguration.current.locales[0].language == Locale.ENGLISH.language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.skip_reason_title)) },
        text = {
            Column {
                SkipReason.entries.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Text(
                            text = if (isEnglish) reason.displayNameEn else reason.displayNameTr,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedReason) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
