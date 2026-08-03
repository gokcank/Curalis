package com.gokcank.curalis.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gokcank.curalis.core.navigation.NavGraph
import com.gokcank.curalis.core.theme.CuralisTheme
import com.gokcank.curalis.core.theme.ThemeController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Modern Android 15+ edge-to-edge support
        enableEdgeToEdge()
        
        ThemeController.init(this)
        
        setContent {
            val darkThemeOverride by ThemeController.isDarkMode.collectAsState()
            val isDark = darkThemeOverride ?: isSystemInDarkTheme()
            
            CuralisTheme(darkTheme = isDark) {
                NavGraph()
            }
        }
    }
}
