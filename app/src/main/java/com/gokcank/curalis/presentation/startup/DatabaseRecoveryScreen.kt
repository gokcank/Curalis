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
fun DatabaseRecoveryScreen(onResetClick: () -> Unit) {
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
            Button(
                onClick = onResetClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.db_recovery_reset_button))
            }
        }
    }
}
