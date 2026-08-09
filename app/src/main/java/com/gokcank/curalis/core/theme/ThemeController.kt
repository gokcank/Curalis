package com.gokcank.curalis.core.theme

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(
        if (prefs.contains(KEY_IS_DARK_MODE)) prefs.getBoolean(KEY_IS_DARK_MODE, false) else null
    )
    val isDarkMode = _isDarkMode.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        _isDarkMode.value = isDark
    }

    fun resetToSystem() {
        prefs.edit().remove(KEY_IS_DARK_MODE).apply()
        _isDarkMode.value = null
    }

    companion object {
        private const val PREFS_NAME = "curalis_theme_prefs"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
    }
}
