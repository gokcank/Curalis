package com.gokcank.curalis.presentation.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.MedicationShape
import com.gokcank.curalis.domain.model.shape

/**
 * İlaç şekli simgeleri. Uygulamaya özgü, elle çizilmiş dolu (filled) simgeler; Material Filled
 * ailesiyle aynı 24×24 ızgara ve tek renk kullanılır, renk çizim yerinde verilir (tint).
 * Boşluklar (çentik, etiket, pencere) "even-odd" dolgu ile oyulur ki tek renkte de okunsun.
 */
fun MedicationShape.icon(): ImageVector = ShapeIcons.forShape(this)

/** Ekranda ilacın şekli: seçilen ya da formdan türetilen. */
fun Medication.shapeIcon(): ImageVector = shape.icon()

/** Yalnızca form bilinen yerler için (ör. ilaç kaydı bulunamadıysa). */
fun MedicationForm.shapeIcon(): ImageVector = MedicationShape.defaultFor(this).icon()

@StringRes
fun MedicationShape.labelRes(): Int = when (this) {
    MedicationShape.ROUND_TABLET -> R.string.shape_round_tablet
    MedicationShape.OVAL_TABLET -> R.string.shape_oval_tablet
    MedicationShape.CAPSULE -> R.string.shape_capsule
    MedicationShape.DROP -> R.string.shape_drop
    MedicationShape.BOTTLE -> R.string.shape_bottle
    MedicationShape.SYRINGE -> R.string.shape_syringe
    MedicationShape.TUBE -> R.string.shape_tube
    MedicationShape.INHALER -> R.string.shape_inhaler
    MedicationShape.PATCH -> R.string.shape_patch
    MedicationShape.SACHET -> R.string.shape_sachet
}

private object ShapeIcons {

    private val cache = HashMap<MedicationShape, ImageVector>()

    fun forShape(shape: MedicationShape): ImageVector = synchronized(cache) {
        cache.getOrPut(shape) { build(shape) }
    }

    /** Bir parça: yol verisi ve dolgu kuralı. */
    private class Part(val d: String, val evenOdd: Boolean = false)

    private fun build(shape: MedicationShape): ImageVector = when (shape) {
        // Hafif yukarıdan bakılan yuvarlak tablet: üst yüz ve altında ince bir kenar. Düz bir
        // daire + yatay çizgi küçük boyutta "giriş yok" işaretine benziyordu.
        MedicationShape.ROUND_TABLET -> vector(
            "shape_round_tablet",
            Part("M3,10 A9,5.5 0 1,1 21,10 A9,5.5 0 1,1 3,10 Z"),
            Part("M3,11.4 V13.8 A9,5.5 0 0,0 21,13.8 V11.4 A9,5.5 0 0,1 3,11.4 Z")
        )
        // Çentikli, yatık oval tablet.
        MedicationShape.OVAL_TABLET -> vector(
            "shape_oval_tablet",
            Part("M2.5,12 A9.5,6 0 1,1 21.5,12 A9.5,6 0 1,1 2.5,12 Z M6.5,11.2 H17.5 V12.8 H6.5 Z", evenOdd = true),
            rotate = -30f
        )
        // İki yarısı arasında ince bir boşluk olan çapraz kapsül; sol yarı içi boş.
        MedicationShape.CAPSULE -> vector(
            "shape_capsule",
            Part("M6.5,8 H11.3 V16 H6.5 A4,4 0 0,1 6.5,8 Z M6.5,9.6 A2.4,2.4 0 0,0 6.5,14.4 H9.7 V9.6 Z", evenOdd = true),
            Part("M12.7,8 H17.5 A4,4 0 0,1 17.5,16 H12.7 Z"),
            rotate = -45f
        )
        MedicationShape.DROP -> vector(
            "shape_drop",
            Part("M12,2.5 C12,2.5 5,10.2 5,14.8 A7,7 0 0,0 19,14.8 C19,10.2 12,2.5 12,2.5 Z")
        )
        // Kapaklı, etiketli şurup şişesi.
        MedicationShape.BOTTLE -> vector(
            "shape_bottle",
            Part("M9,2 H15 V5 H9 Z"),
            Part(
                "M8.5,6 H15.5 V8 C17.8,8.9 19,10.6 19,12.8 V20 A2,2 0 0,1 17,22 H7 A2,2 0 0,1 5,20 V12.8 " +
                    "C5,10.6 6.2,8.9 8.5,8 Z M8,13 H16 V18.5 H8 Z",
                evenOdd = true
            )
        )
        // Çapraz şırınga: piston başı, çubuk, pencereli gövde, uç ve iğne.
        MedicationShape.SYRINGE -> vector(
            "shape_syringe",
            Part("M1.5,9.5 H3.3 V14.5 H1.5 Z"),
            Part("M3.3,11.2 H6 V12.8 H3.3 Z"),
            Part("M6,8.5 H7.4 V15.5 H6 Z"),
            Part("M7.4,9.2 H17 V14.8 H7.4 Z M9,10.8 H15.4 V13.2 H9 Z", evenOdd = true),
            Part("M17,10.6 H19 V13.4 H17 Z"),
            Part("M19,11.55 H22.8 V12.45 H19 Z"),
            rotate = -45f
        )
        // Kıvrık uçlu, kapaklı krem tüpü; çapraz durur, kapak sağ üstte.
        MedicationShape.TUBE -> vector(
            "shape_tube",
            Part("M6.5,2.5 H17.5 V4.3 H6.5 Z"),
            Part("M7,5 H17 L15.4,16.2 H8.6 Z"),
            Part("M10.3,16.2 H13.7 V18 H10.3 Z"),
            Part("M9.2,18.6 H14.8 V21.5 H9.2 Z"),
            rotate = -135f
        )
        // Üstte kartuş; altında sola uzanan ağızlıklı gövde.
        MedicationShape.INHALER -> vector(
            "shape_inhaler",
            Part("M12.7,2 H17.3 A0.8,0.8 0 0,1 18.1,2.8 V7.6 H11.9 V2.8 A0.8,0.8 0 0,1 12.7,2 Z"),
            Part("M11.5,8.5 H18.5 V19 A2,2 0 0,1 16.5,21 H7.5 A2,2 0 0,1 5.5,19 V16.5 A1.5,1.5 0 0,1 7,15 H11.5 Z")
        )
        // Ortası pedli, çapraz yara bandı.
        MedicationShape.PATCH -> vector(
            "shape_patch",
            Part("M5.5,8.5 H18.5 A3.5,3.5 0 0,1 18.5,15.5 H5.5 A3.5,3.5 0 0,1 5.5,8.5 Z M8.8,9.8 H15.2 V14.2 H8.8 Z", evenOdd = true),
            Part("M10,11 H14 V13 H10 Z"),
            rotate = -45f
        )
        // Tırtıklı kenarlı toz saşesi.
        MedicationShape.SACHET -> vector(
            "shape_sachet",
            Part(
                "M5,5 L6.75,3 L8.5,5 L10.25,3 L12,5 L13.75,3 L15.5,5 L17.25,3 L19,5 V19 L17.25,21 L15.5,19 " +
                    "L13.75,21 L12,19 L10.25,21 L8.5,19 L6.75,21 L5,19 Z M9,12 A3,3 0 1,1 15,12 A3,3 0 1,1 9,12 Z",
                evenOdd = true
            )
        )
    }

    private fun vector(name: String, vararg parts: Part, rotate: Float = 0f): ImageVector {
        val builder = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        )
        builder.addGroup(name = "body", rotate = rotate, pivotX = 12f, pivotY = 12f)
        parts.forEach { part ->
            builder.addPath(
                pathData = addPathNodes(part.d),
                pathFillType = if (part.evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
                fill = SolidColor(Color.Black)
            )
        }
        builder.clearGroup()
        return builder.build()
    }
}
