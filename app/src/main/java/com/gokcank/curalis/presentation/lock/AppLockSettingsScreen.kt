package com.gokcank.curalis.presentation.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.security.BiometricAuthenticator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppLockSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDisableConfirm by remember { mutableStateOf(false) }

    val biometricAvailable = remember { BiometricAuthenticator.isAvailable(context) }

    val lockEnabledMessage = stringResource(R.string.app_lock_pin_set)
    val pinChangedMessage = stringResource(R.string.app_lock_pin_changed)
    val lockRemovedMessage = stringResource(R.string.app_lock_removed)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(
                when (message) {
                    AppLockMessage.LOCK_ENABLED -> lockEnabledMessage
                    AppLockMessage.PIN_CHANGED -> pinChangedMessage
                    AppLockMessage.LOCK_DISABLED -> lockRemovedMessage
                }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_lock), color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // PIN girişi yarıdaysa önce ona geri dön, ekrandan çıkma.
                            if (uiState.stage != PinEntryStage.NONE) viewModel.cancelPinSetup()
                            else onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.stage != PinEntryStage.NONE) {
            PinSetupContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                stage = uiState.stage,
                enteredCount = uiState.enteredPin.length,
                pinLength = uiState.pinLength,
                isMismatch = uiState.isMismatch,
                onDigitClick = viewModel::onDigitEntered,
                onBackspaceClick = viewModel::onBackspace
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!uiState.isLockEnabled) {
                Text(
                    text = stringResource(R.string.app_lock_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_lock_turn_on_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = viewModel::startPinSetup,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.app_lock_turn_on))
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.app_lock_biometric_toggle),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (biometricAvailable) {
                                    stringResource(R.string.app_lock_biometric_toggle_desc)
                                } else {
                                    stringResource(R.string.app_lock_biometric_unavailable)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isBiometricEnabled && biometricAvailable,
                            onCheckedChange = viewModel::setBiometricEnabled,
                            enabled = biometricAvailable
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = viewModel::startPinSetup,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(R.string.app_lock_change_pin))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showDisableConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.app_lock_turn_off),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDisableConfirm) {
        AlertDialog(
            onDismissRequest = { showDisableConfirm = false },
            title = { Text(stringResource(R.string.app_lock_turn_off_confirm_title)) },
            text = { Text(stringResource(R.string.app_lock_turn_off_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.disableLock()
                        showDisableConfirm = false
                    }
                ) {
                    Text(
                        stringResource(R.string.app_lock_turn_off),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PinSetupContent(
    modifier: Modifier,
    stage: PinEntryStage,
    enteredCount: Int,
    pinLength: Int,
    isMismatch: Boolean,
    onDigitClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (stage) {
                PinEntryStage.CONFIRM -> stringResource(R.string.app_lock_confirm_pin)
                else -> stringResource(R.string.app_lock_create_pin, pinLength)
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        PinDots(
            enteredCount = enteredCount,
            pinLength = pinLength,
            isError = isMismatch
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mesaj görünse de görünmese de aynı yer ayrılır ki tuş takımı yerinden oynamasın.
        Text(
            text = if (isMismatch) stringResource(R.string.app_lock_pin_mismatch) else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(40.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PinKeypad(
            onDigitClick = onDigitClick,
            onBackspaceClick = onBackspaceClick
        )
    }
}
