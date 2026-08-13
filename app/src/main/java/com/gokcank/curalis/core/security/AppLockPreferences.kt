package com.gokcank.curalis.core.security

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama kilidi tercihlerini saklar. [com.gokcank.curalis.core.notification.NotificationPreferences]
 * ile aynı desende basit SharedPreferences kullanılıyor; burada saklanan tek hassas değer olan PIN,
 * düz metin değil [PinHasher] ile üretilmiş geri döndürülemez bir özet.
 */
@Singleton
class AppLockPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Kilit yalnızca bir PIN belirlenmişse gerçekten etkindir. */
    val isLockEnabled: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    private val _lockEnabledState = MutableStateFlow(isLockEnabled)

    /** Ayarlar ekranının kilit durumunu anlık takip edebilmesi için. */
    val lockEnabledState = _lockEnabledState.asStateFlow()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_BIOMETRIC_ENABLED, value) }

    fun setPin(pin: String) {
        prefs.edit { putString(KEY_PIN_HASH, PinHasher.hash(pin)) }
        _lockEnabledState.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return PinHasher.verify(pin, stored)
    }

    /** Kilidi tamamen kaldırır; biyometrik tercihi de PIN'siz anlamsız olduğu için sıfırlanır. */
    fun clearLock() {
        prefs.edit {
            remove(KEY_PIN_HASH)
            remove(KEY_BIOMETRIC_ENABLED)
        }
        _lockEnabledState.value = false
    }

    companion object {
        private const val PREFS_NAME = "curalis_app_lock_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val PIN_LENGTH = 4
    }
}
