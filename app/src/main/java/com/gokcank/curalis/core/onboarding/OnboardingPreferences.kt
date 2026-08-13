package com.gokcank.curalis.core.onboarding

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * İlk kurulum karşılama akışının daha önce tamamlanıp tamamlanmadığını saklar.
 * Uygulama hesap gerektirmediği için burada sunucuya senkronize edilecek bir şey yok —
 * bu tek bayrak, yalnızca bu cihaza özel, kalıcı bir yerel tercih.
 */
@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_COMPLETED, value) }

    companion object {
        private const val PREFS_NAME = "curalis_onboarding_prefs"
        private const val KEY_COMPLETED = "has_completed_onboarding"
    }
}
