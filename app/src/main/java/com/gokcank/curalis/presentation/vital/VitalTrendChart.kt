package com.gokcank.curalis.presentation.vital

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.model.VitalType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Seçili bir ölçüm türünün zaman içindeki değişimini gösteren basit bir çizgi grafik.
 * Üçüncü taraf bir grafik kütüphanesi kullanmaz; Compose'un kendi çizim yüzeyi (Canvas)
 * üzerine kurulu. Tansiyon için sistolik/diyastolik aynı eksende iki ayrı çizgi olarak
 * gösterilir (aynı birim, mmHg); diğer tüm türlerde tek çizgi vardır.
 */
@Composable
fun VitalTrendChart(vitals: List<Vital>, type: VitalType) {
    val sorted = remember(vitals) { vitals.sortedBy { it.timeInMillis } }
    if (sorted.size < 2) return

    val isBloodPressure = type == VitalType.BLOOD_PRESSURE
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedIndex by remember(sorted) { mutableIntStateOf(sorted.lastIndex) }
    val selected = sorted[selectedIndex.coerceIn(0, sorted.lastIndex)]

    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Seçili noktanın değeri, doğrudan bir sayı olarak — grafiğin üstündeki "başlık".
            val selectedValueText = if (isBloodPressure && selected.value2 != null) {
                "${selected.value1.toInt()}/${selected.value2.toInt()} ${selected.unit}"
            } else {
                "${formatValue(selected.value1)} ${selected.unit}"
            }
            Text(
                text = selectedValueText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dateFormat.format(Date(selected.timeInMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )

            if (isBloodPressure) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendEntry(color = primaryColor, label = stringResource(R.string.vital_systolic))
                    LegendEntry(color = secondaryColor, label = stringResource(R.string.vital_diastolic))
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))

            val allValues = if (isBloodPressure) {
                sorted.flatMap { listOfNotNull(it.value1, it.value2) }
            } else {
                sorted.map { it.value1 }
            }
            val maxValue = allValues.max()
            val minValue = allValues.min()
            // Aralık tamamen düzse (tek bir değer tekrarlanmışsa) grafiğin dümdüz bir
            // çizgiye sıkışmaması için küçük bir pay eklenir.
            val range = (maxValue - minValue).let { if (it < 0.0001) 1.0 else it }
            val paddedMin = minValue - range * 0.15
            val paddedMax = maxValue + range * 0.15
            val paddedRange = paddedMax - paddedMin

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .pointerInput(sorted) {
                        detectTapGestures { offset ->
                            if (sorted.size < 2) return@detectTapGestures
                            val stepX = size.width / (sorted.size - 1).toFloat()
                            val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, sorted.lastIndex)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val stepX = if (sorted.size > 1) this.size.width / (sorted.size - 1) else 0f

                fun yFor(value: Double): Float {
                    val ratio = ((value - paddedMin) / paddedRange).toFloat()
                    return this.size.height * (1f - ratio)
                }

                // Yatay ızgara — 3 çizgi, göz alıcı olmayan (recessive) bir renkte.
                val gridLineCount = 3
                repeat(gridLineCount) { i ->
                    val y = this.size.height * i / (gridLineCount - 1).toFloat()
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(this.size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                fun drawSeries(values: List<Double>, color: Color) {
                    val points = values.mapIndexed { index, value -> Offset(index * stepX, yFor(value)) }
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = color,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    points.forEachIndexed { index, point ->
                        val isSelected = index == selectedIndex
                        drawCircle(
                            color = color,
                            radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                            center = point
                        )
                        if (isSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }

                drawSeries(sorted.map { it.value1 }, primaryColor)
                if (isBloodPressure) {
                    drawSeries(sorted.mapNotNull { it.value2 }, secondaryColor)
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(sorted.first().timeInMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    text = dateFormat.format(Date(sorted.last().timeInMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}

@Composable
private fun LegendEntry(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
