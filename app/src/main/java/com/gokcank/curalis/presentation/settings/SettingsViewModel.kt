package com.gokcank.curalis.presentation.settings

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.security.AppLockPreferences
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.core.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController,
    appLockPreferences: AppLockPreferences
) : ViewModel() {

    val themeMode = themeController.themeMode

    val isAppLockEnabled = appLockPreferences.lockEnabledState

    fun setThemeMode(mode: ThemeMode) = themeController.setThemeMode(mode)
}
