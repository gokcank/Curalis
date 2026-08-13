package com.gokcank.curalis.presentation.lock

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.security.AppLockController
import com.gokcank.curalis.core.security.AppLockPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Kilit ayarları ekranının hangi aşamada olduğu. */
enum class PinEntryStage {
    /** PIN girişi yok; mevcut ayarlar listeleniyor. */
    NONE,

    /** Yeni PIN'in ilk kez girilmesi. */
    CREATE,

    /** Aynı PIN'in doğrulama amacıyla ikinci kez girilmesi. */
    CONFIRM
}

data class AppLockSettingsUiState(
    val isLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val stage: PinEntryStage = PinEntryStage.NONE,
    val enteredPin: String = "",
    val pinLength: Int = AppLockPreferences.PIN_LENGTH,
    val isMismatch: Boolean = false
)

@HiltViewModel
class AppLockSettingsViewModel @Inject constructor(
    private val preferences: AppLockPreferences,
    private val appLockController: AppLockController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AppLockSettingsUiState(
            isLockEnabled = preferences.isLockEnabled,
            isBiometricEnabled = preferences.isBiometricEnabled
        )
    )
    val uiState = _uiState.asStateFlow()

    /** Ekranda kısa bilgi mesajı göstermek için (PIN kuruldu/değişti/kaldırıldı). */
    private val _messages = MutableSharedFlow<AppLockMessage>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    private var firstEntry: String? = null

    fun startPinSetup() {
        firstEntry = null
        _uiState.update {
            it.copy(stage = PinEntryStage.CREATE, enteredPin = "", isMismatch = false)
        }
    }

    fun cancelPinSetup() {
        firstEntry = null
        _uiState.update {
            it.copy(stage = PinEntryStage.NONE, enteredPin = "", isMismatch = false)
        }
    }

    fun onDigitEntered(digit: Int) {
        val state = _uiState.value
        if (state.stage == PinEntryStage.NONE) return
        if (state.enteredPin.length >= state.pinLength) return

        val updated = state.enteredPin + digit
        _uiState.update { it.copy(enteredPin = updated, isMismatch = false) }

        if (updated.length == state.pinLength) {
            when (state.stage) {
                PinEntryStage.CREATE -> {
                    firstEntry = updated
                    _uiState.update { it.copy(stage = PinEntryStage.CONFIRM, enteredPin = "") }
                }
                PinEntryStage.CONFIRM -> confirm(updated)
                PinEntryStage.NONE -> Unit
            }
        }
    }

    fun onBackspace() {
        _uiState.update {
            if (it.enteredPin.isEmpty()) it
            else it.copy(enteredPin = it.enteredPin.dropLast(1), isMismatch = false)
        }
    }

    private fun confirm(secondEntry: String) {
        val wasEnabled = preferences.isLockEnabled
        if (firstEntry != secondEntry) {
            // Eşleşmezse baştan başlanır; yarım kalmış bir PIN kaydedilmemeli.
            firstEntry = null
            _uiState.update {
                it.copy(stage = PinEntryStage.CREATE, enteredPin = "", isMismatch = true)
            }
            return
        }

        preferences.setPin(secondEntry)
        firstEntry = null
        appLockController.onLockSettingsChanged()
        _uiState.update {
            it.copy(
                stage = PinEntryStage.NONE,
                enteredPin = "",
                isMismatch = false,
                isLockEnabled = true
            )
        }
        _messages.tryEmit(if (wasEnabled) AppLockMessage.PIN_CHANGED else AppLockMessage.LOCK_ENABLED)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.isBiometricEnabled = enabled
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun disableLock() {
        preferences.clearLock()
        appLockController.onLockSettingsChanged()
        _uiState.update {
            it.copy(isLockEnabled = false, isBiometricEnabled = false, stage = PinEntryStage.NONE)
        }
        _messages.tryEmit(AppLockMessage.LOCK_DISABLED)
    }
}

enum class AppLockMessage { LOCK_ENABLED, PIN_CHANGED, LOCK_DISABLED }
