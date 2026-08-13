package com.gokcank.curalis.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulamanın o an kilitli olup olmadığını tutar.
 *
 * Kilit durumu kalıcı olarak saklanmaz; uygulama her açıldığında (soğuk başlangıç) kilitli
 * başlar ve arka plana alınıp geri dönüldüğünde yeniden kilitlenir. Böylece "kilit açıldı"
 * bilgisi cihazda hiçbir yerde bırakılmaz.
 */
@Singleton
class AppLockController @Inject constructor(
    private val preferences: AppLockPreferences
) {
    private val _isLocked = MutableStateFlow(preferences.isLockEnabled)
    val isLocked = _isLocked.asStateFlow()

    private var backgroundedAtMillis: Long? = null

    fun unlock() {
        _isLocked.value = false
        backgroundedAtMillis = null
    }

    /** Kilit ilk kez kurulduğunda veya kaldırıldığında çağrılır; kullanıcı zaten uygulamanın içindedir. */
    fun onLockSettingsChanged() {
        _isLocked.value = false
        backgroundedAtMillis = null
    }

    fun onAppBackgrounded() {
        if (!preferences.isLockEnabled) return
        // Zaten kilitliyse zamanı sıfırlamayalım; aksi halde kilit ekranındayken bildirim
        // gölgesini açıp kapatmak sayacı sürekli ileri atardı.
        if (_isLocked.value) return
        backgroundedAtMillis = System.currentTimeMillis()
    }

    fun onAppForegrounded() {
        if (!preferences.isLockEnabled) {
            _isLocked.value = false
            return
        }
        val backgroundedAt = backgroundedAtMillis ?: return
        if (System.currentTimeMillis() - backgroundedAt >= GRACE_PERIOD_MILLIS) {
            _isLocked.value = true
        }
        backgroundedAtMillis = null
    }

    companion object {
        /**
         * Kısa bir tolerans süresi: yedekleme ekranının açtığı dosya seçici, kamera veya
         * izin penceresi de uygulamayı teknik olarak arka plana alır. Tolerans olmasaydı
         * kullanıcı her dosya seçiminden sonra PIN girmek zorunda kalırdı.
         */
        private const val GRACE_PERIOD_MILLIS = 30_000L
    }
}
