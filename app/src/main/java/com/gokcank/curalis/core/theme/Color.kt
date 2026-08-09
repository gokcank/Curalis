package com.gokcank.curalis.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Curalis renk sistemi.
 *
 * design-system.md kuralları:
 * - Her rengin tanımlı bir anlamı vardır; dekoratif renk kullanılmaz.
 * - Error SADECE hata/doğrulama hatası/tehlikeli eylem içindir. Sıradan dikkat için kullanılmaz.
 * - Dikkat gerektiren ama tehlikeli olmayan durumlar (düşük stok vb.) Warning kullanır.
 * - Koyu tema saf siyah değildir.
 * - Tüm metin/zemin çiftleri WCAG AA (küçük metin 4.5:1) hedefiyle seçilmiştir.
 *
 * Not: Buradaki değerler Material 3 şemasının TAMAMINI doldurur. Eksik bırakılan her slot
 * Material'ın varsayılan mor/lila paletine düşer — daha önce açık temada kartların lilaya
 * dönmesinin sebebi buydu.
 */

// ---------- Açık tema ----------
val LightPrimary = Color(0xFF1565C0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD6E4F7)
val LightOnPrimaryContainer = Color(0xFF0A2F5C)

val LightSecondary = Color(0xFF00695C)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFCDE8E4)
val LightOnSecondaryContainer = Color(0xFF00302A)

val LightTertiary = Color(0xFF44546A)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFDFE4EC)
val LightOnTertiaryContainer = Color(0xFF1E2733)

val LightBackground = Color(0xFFF7F9FC)
val LightOnBackground = Color(0xFF1A1C1E)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1C1E)
val LightSurfaceVariant = Color(0xFFE7EAEE)
val LightOnSurfaceVariant = Color(0xFF43474E)
val LightOutline = Color(0xFF6F737B)
val LightOutlineVariant = Color(0xFFC3C7CF)

val LightError = Color(0xFFB3261E)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF9DEDC)
val LightOnErrorContainer = Color(0xFF5C1710)

// ---------- Koyu tema (saf siyah kullanılmaz) ----------
val DarkPrimary = Color(0xFFA6C8F5)
val DarkOnPrimary = Color(0xFF08305F)
val DarkPrimaryContainer = Color(0xFF25487A)
val DarkOnPrimaryContainer = Color(0xFFD6E4F7)

val DarkSecondary = Color(0xFF7FCCC2)
val DarkOnSecondary = Color(0xFF00352E)
val DarkSecondaryContainer = Color(0xFF004E45)
val DarkOnSecondaryContainer = Color(0xFFCDE8E4)

val DarkTertiary = Color(0xFFBCC7D8)
val DarkOnTertiary = Color(0xFF26303D)
val DarkTertiaryContainer = Color(0xFF3D4757)
val DarkOnTertiaryContainer = Color(0xFFDFE4EC)

val DarkBackground = Color(0xFF121417)
val DarkOnBackground = Color(0xFFE3E2E6)
val DarkSurface = Color(0xFF1A1D21)
val DarkOnSurface = Color(0xFFE3E2E6)
val DarkSurfaceVariant = Color(0xFF43474E)
val DarkOnSurfaceVariant = Color(0xFFC3C7CF)
val DarkOutline = Color(0xFF8D9199)
val DarkOutlineVariant = Color(0xFF43474E)

val DarkError = Color(0xFFF2B8B5)
val DarkOnError = Color(0xFF601410)
val DarkErrorContainer = Color(0xFF8C1D18)
val DarkOnErrorContainer = Color(0xFFF9DEDC)

/**
 * Material 3'te karşılığı olmayan ama design-system.md'nin zorunlu kıldığı anlamsal renkler.
 * [LocalCuralisColors] üzerinden okunur.
 */
data class CuralisSemanticColors(
    /** Dikkat gerektiren ama tehlikeli olmayan durum: düşük stok, yaklaşan yenileme, eksik bilgi. */
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    /** Tamamlanan kullanıcı eylemi. Tıbbi başarı anlamına ASLA gelmez. */
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    /** Nötr bilgilendirme: ipuçları, açıklamalar. */
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color
)

val LightSemanticColors = CuralisSemanticColors(
    warning = Color(0xFF8A5300),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDDB0),
    onWarningContainer = Color(0xFF2C1700),
    success = Color(0xFF1B6B3A),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFC6EBD3),
    onSuccessContainer = Color(0xFF04220F),
    info = Color(0xFF00629D),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFCDE5FF),
    onInfoContainer = Color(0xFF001D31)
)

val DarkSemanticColors = CuralisSemanticColors(
    warning = Color(0xFFFFB95C),
    onWarning = Color(0xFF472A00),
    warningContainer = Color(0xFF6B4000),
    onWarningContainer = Color(0xFFFFDDB0),
    success = Color(0xFF86D9A4),
    onSuccess = Color(0xFF00391C),
    successContainer = Color(0xFF05512A),
    onSuccessContainer = Color(0xFFC6EBD3),
    info = Color(0xFF96CCFF),
    onInfo = Color(0xFF003354),
    infoContainer = Color(0xFF004A77),
    onInfoContainer = Color(0xFFCDE5FF)
)

/**
 * Kullanıcının ilaca atadığı ayırt edici renkler. Bunlar anlamsal değil, yalnızca
 * kullanıcının ilaçlarını birbirinden ayırması içindir; bu yüzden anlamsal
 * paletten ayrı tutulur ve durum bildirmek için kullanılmaz.
 */
val MedicationAccentColors = listOf(
    "#1565C0", // Mavi
    "#00695C", // Teal
    "#6A4CA8", // Mor
    "#AD5B00", // Turuncu
    "#1B6B3A", // Yeşil
    "#9B2C6F", // Magenta
    "#44546A"  // Gri-mavi
)
