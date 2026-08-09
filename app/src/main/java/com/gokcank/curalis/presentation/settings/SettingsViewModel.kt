package com.gokcank.curalis.presentation.settings

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.theme.ThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController
) : ViewModel() {

    val isDarkMode = themeController.isDarkMode

    fun setDarkMode(isDark: Boolean) = themeController.setDarkMode(isDark)

    fun resetThemeToSystem() = themeController.resetToSystem()
}
