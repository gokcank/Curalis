package com.gokcank.curalis.presentation.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.security.BiometricAuthenticator

/**
 * Uygulama açılırken gösterilen kilit ekranı. Geri tuşuyla atlanamaz; kilidi yalnızca
 * doğru PIN veya biyometrik doğrulama kaldırır.
 */
@Composable
fun AppLockScreen(
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val biometricTitle = stringResource(R.string.app_lock_biometric_title)
    val biometricSubtitle = stringResource(R.string.app_lock_biometric_subtitle)
    val biometricNegative = stringResource(R.string.app_lock_enter_pin)

    // ViewModel kilit açıldıktan sonra da yaşadığı için, ekran her göründüğünde önceki
    // girişin izleri temizlenir ve biyometrik tercihi yeniden okunur.
    LaunchedEffect(Unit) { viewModel.onScreenShown() }

    val canUseBiometric = uiState.isBiometricEnabled &&
        activity != null &&
        BiometricAuthenticator.isAvailable(context)

    fun promptBiometric() {
        val host = activity ?: return
        BiometricAuthenticator.authenticate(
            activity = host,
            title = biometricTitle,
            subtitle = biometricSubtitle,
            negativeButtonText = biometricNegative,
            onSuccess = viewModel::onBiometricSuccess,
            onFailure = { /* Kullanıcı PIN girişiyle devam eder. */ }
        )
    }

    // Biyometrik açıksa ekran görünür görünmez sorulur; kullanıcı vazgeçerse PIN kalır.
    LaunchedEffect(canUseBiometric) {
        if (canUseBiometric) promptBiometric()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_lock_enter_pin),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            PinDots(
                enteredCount = uiState.enteredPin.length,
                pinLength = uiState.pinLength,
                isError = uiState.isWrongPin
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Yükseklik her durumda ayrılır ki hata mesajı belirince tuş takımı zıplamasın.
            Text(
                text = when {
                    uiState.lockoutSecondsRemaining > 0 -> pluralStringResource(
                        R.plurals.app_lock_too_many_attempts,
                        uiState.lockoutSecondsRemaining,
                        uiState.lockoutSecondsRemaining
                    )
                    uiState.isWrongPin -> stringResource(R.string.app_lock_wrong_pin)
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            PinKeypad(
                onDigitClick = viewModel::onDigitEntered,
                onBackspaceClick = viewModel::onBackspace,
                enabled = uiState.lockoutSecondsRemaining == 0,
                onBiometricClick = if (canUseBiometric) ::promptBiometric else null
            )
        }
    }
}
