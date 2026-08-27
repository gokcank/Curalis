package com.gokcank.curalis.core.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Bölüm kimliği renkleri: önce yalnızca HomeScreen.kt içindeydi (design-system.md'nin
 * "dekoratif renk kullanılmaz" kuralına bilinçli istisna — burada renk dekoratif değil,
 * bölümü tanımlayan bir kimlik). Tasarım geçişi planının Faz 0'ı gereği, liste/hub
 * ekranlarının hepsinin aynı kimlikten beslenmesi için buraya taşındı.
 */
val SectionGradientTop = Color(0xFFE9F1FE)
val SectionGradientMid = Color(0xFFEAF6EE)
val SectionGradientBottom = Color(0xFFFDF3E4)
val SectionStreakFlame = Color(0xFFE8834A)
val SectionAdherenceGreen = Color(0xFF2E7D6B)
val SectionAccentMedications = Color(0xFF3B6FE0)
val SectionAccentDoctors = Color(0xFF4F46E5)
val SectionAccentAppointments = Color(0xFF9333EA)
val SectionAccentVitals = Color(0xFFDC2626)
val SectionAccentSymptoms = Color(0xFFD97706)
val SectionAccentNotes = Color(0xFF0F766E)

/**
 * Açık temada bölüm kimliğinin yumuşak gradyan zeminini, koyu/AMOLED'de düz zemini döndürür.
 * Gradyan koyu temada iki ucu birbirine çok yakın olup 8-bit bantlanma yaratabilir ve
 * AMOLED'in pil avantajı düz siyahtan geldiği için yalnızca açık temada kullanılır.
 */
@Composable
fun sectionBackgroundModifier(themeMode: ThemeMode): Modifier =
    if (themeMode == ThemeMode.LIGHT) {
        Modifier.background(
            Brush.verticalGradient(listOf(SectionGradientTop, SectionGradientMid, SectionGradientBottom))
        )
    } else {
        Modifier.background(MaterialTheme.colorScheme.background)
    }

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface ThemeControllerEntryPoint {
    fun themeController(): ThemeController
}

/**
 * ViewModel'i değiştirmeden ekranın geçerli [ThemeMode]'unu okumak için — liste/hub
 * ekranlarının çoğu ThemeController'ı zaten inject etmiyordu, her birine ayrı ayrı
 * eklemek yerine PdfReportGeneratorEntryPoint'teki gibi doğrudan EntryPoint kullanılır.
 */
@Composable
fun rememberSectionThemeMode(): State<ThemeMode> {
    val context = LocalContext.current
    val themeController = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ThemeControllerEntryPoint::class.java
        ).themeController()
    }
    return themeController.themeMode.collectAsState()
}
