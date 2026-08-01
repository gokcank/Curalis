package com.gokcank.curalis.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gokcank.curalis.core.theme.Typography

@Composable
fun MainScreen() {
    // Edge-to-edge is handled by Scaffold automatically padding inner content
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to Curalis",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}
