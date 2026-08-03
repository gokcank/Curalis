package com.gokcank.curalis.core.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeController {
    private const val PREFS_NAME = "curalis_theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_IS_DARK_MODE)) {
            _isDarkMode.value = prefs.getBoolean(KEY_IS_DARK_MODE, false)
        }
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        _isDarkMode.value = isDark
    }

    fun resetToSystem(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IS_DARK_MODE).apply()
        _isDarkMode.value = null
    }
}
