package com.gokcank.curalis.presentation.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.security.AppLockController
import com.gokcank.curalis.core.security.AppLockPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLockUiState(
    val enteredPin: String = "",
    val pinLength: Int = AppLockPreferences.PIN_LENGTH,
    val isWrongPin: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    /** Sıfırdan büyükse giriş kilitlidir ve bu kadar saniye beklenmelidir. */
    val lockoutSecondsRemaining: Int = 0
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val preferences: AppLockPreferences,
    private val appLockController: AppLockController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AppLockUiState(isBiometricEnabled = preferences.isBiometricEnabled)
    )
    val uiState = _uiState.asStateFlow()

    private var failedAttempts = 0
    private var lockoutJob: Job? = null

    /**
     * Kilit ekranı her göründüğünde çağrılır.
     *
     * Bu ViewModel Activity'ye bağlıdır ve kilit açıldıktan sonra da yaşamaya devam eder;
     * temizlenmezse bir sonraki kilitlenmede önceki girişin haneleri dolu görünür ve
     * [onDigitEntered] "giriş zaten tamamlanmış" sayıp bütün dokunuşları yok sayardı.
     * Biyometrik tercihi de bu arada ayarlardan değişmiş olabilir, birlikte tazelenir.
     */
    fun onScreenShown() {
        _uiState.update {
            it.copy(
                enteredPin = "",
                isWrongPin = false,
                isBiometricEnabled = preferences.isBiometricEnabled
            )
        }
    }

    fun onDigitEntered(digit: Int) {
        val state = _uiState.value
        if (state.lockoutSecondsRemaining > 0) return
        if (state.enteredPin.length >= state.pinLength) return

        val updated = state.enteredPin + digit
        _uiState.update { it.copy(enteredPin = updated, isWrongPin = false) }

        if (updated.length == state.pinLength) {
            verify(updated)
        }
    }

    fun onBackspace() {
        _uiState.update {
            if (it.lockoutSecondsRemaining > 0 || it.enteredPin.isEmpty()) it
            else it.copy(enteredPin = it.enteredPin.dropLast(1), isWrongPin = false)
        }
    }

    fun onBiometricSuccess() {
        failedAttempts = 0
        _uiState.update { it.copy(enteredPin = "", isWrongPin = false) }
        appLockController.unlock()
    }

    private fun verify(pin: String) {
        if (preferences.verifyPin(pin)) {
            failedAttempts = 0
            _uiState.update { it.copy(enteredPin = "", isWrongPin = false) }
            appLockController.unlock()
            return
        }

        failedAttempts++
        _uiState.update { it.copy(enteredPin = "", isWrongPin = true) }
        if (failedAttempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
            startLockout()
        }
    }

    /**
     * Ard arda hatalı denemelerden sonra girişi bir süre kapatır; PIN'ler kısa olduğu için
     * bekleme süresi olmadan tüm kombinasyonlar hızla denenebilirdi.
     */
    private fun startLockout() {
        failedAttempts = 0
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            var remaining = LOCKOUT_SECONDS
            while (remaining > 0) {
                _uiState.update { it.copy(lockoutSecondsRemaining = remaining) }
                delay(1000)
                remaining--
            }
            _uiState.update { it.copy(lockoutSecondsRemaining = 0, isWrongPin = false) }
        }
    }

    companion object {
        private const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        private const val LOCKOUT_SECONDS = 30
    }
}
