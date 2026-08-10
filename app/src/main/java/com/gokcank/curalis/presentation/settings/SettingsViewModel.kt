package com.gokcank.curalis.presentation.settings

import androidx.lifecycle.ViewModel
import com.gokcank.curalis.core.theme.ThemeController
import com.gokcank.curalis.core.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController
) : ViewModel() {

    val themeMode = themeController.themeMode

    fun setThemeMode(mode: ThemeMode) = themeController.setThemeMode(mode)
}
