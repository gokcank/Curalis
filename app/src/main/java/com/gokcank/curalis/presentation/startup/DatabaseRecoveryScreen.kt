package com.gokcank.curalis.presentation.startup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R

/**
 * MainActivity, uygulama açılırken veritabanının gerçekten açılabildiğini (migration
 * zincirinin geçerli olduğunu) doğrular. Bu doğrulama sürerken gösterilen ara ekran.
 */
@Composable
fun DatabaseCheckingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Veritabanı, beklenmedik şekilde (örn. desteklenmeyen kadar eski bir sürümden) açılamadığında
 * gösterilen kurtarma ekranı. Normalde bu ekranın hiç görünmemesi gerekir — güncel sürümler
 * için migration zinciri (bkz. DatabaseMigrations.kt) her zaman tanımlanır ve veritabanının
 * OS seviyesi otomatik yedeklemeden hariç tutulmasıyla (backup_rules.xml) bu senaryonun
 * tetiklenme yolu zaten kapatıldı. Bu ekran, o önlemlere rağmen oluşabilecek beklenmedik bir
 * durum için son bir güvenlik ağıdır: kullanıcıyı süresiz bir çökme döngüsünde bırakmak yerine
 * açık bir bilgilendirme ve tek bir kontrollü kurtarma yolu sunar.
 */
@Composable
fun DatabaseRecoveryScreen(onResetConfirmed: () -> Unit, onExportClick: () -> Unit) {
    // Sıfırlama verileri sildiği için tek dokunuşla yapılmaz; önce onay sorulur (ADR-011).
    var confirmReset by remember { mutableStateOf(false) }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.db_recovery_reset_confirm_title)) },
            text = { Text(stringResource(R.string.db_recovery_reset_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onResetConfirmed()
                }) { Text(stringResource(R.string.db_recovery_reset_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.db_recovery_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.db_recovery_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Önce dosyayı kaydetmek, sıfırlamadan önceki güvenli adımdır.
            Button(
                onClick = onExportClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.db_export_button))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { confirmReset = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.db_recovery_reset_button))
            }
        }
    }
}

/**
 * SQLite veri dosyasını bozuk bulup yeniden oluşturduğunda, bir sonraki açılışta bir kez
 * gösterilir: kullanıcı aksi halde verilerinin neden boş olduğunu bilemezdi.
 */
@Composable
fun DatabaseRescuedDialog(onExportClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.db_rescued_title)) },
        text = { Text(stringResource(R.string.db_rescued_text)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onExportClick) { Text(stringResource(R.string.db_export_button)) } }
    )
}
