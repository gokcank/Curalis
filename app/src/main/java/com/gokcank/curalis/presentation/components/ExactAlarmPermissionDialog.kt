package com.gokcank.curalis.presentation.components

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.gokcank.curalis.R

/**
 * Tam zamanlı alarm izni (SCHEDULE_EXACT_ALARM) verilmemişse gösterilen ortak diyalog.
 * İlaç ve randevu ekleme/düzenleme ekranlarının ikisinde de aynı şekilde kullanılır.
 */
@Composable
fun ExactAlarmPermissionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exact_alarm_dialog_title)) },
        text = { Text(stringResource(R.string.exact_alarm_dialog_message)) },
        confirmButton = {
            TextButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                }
                onDismiss()
            }) {
                Text(stringResource(R.string.exact_alarm_dialog_settings_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
