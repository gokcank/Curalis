package com.gokcank.curalis.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gokcank.curalis.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Son haftaların uyum yüzdesini zaman içinde gösteren basit bir çizgi grafik.
 * VitalTrendChart ile aynı yaklaşım (Compose Canvas, üçüncü taraf kütüphane yok);
 * veri yoksa (2'den az sonuçlanmış hafta) hiçbir şey çizmez.
 */
@Composable
fun AdherenceTrendChart(points: List<WeeklyAdherencePoint>) {
    val resolved = remember(points) { points.filter { it.percentage != null } }
    if (resolved.size < 2) return

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedIndex by remember(resolved) { mutableIntStateOf(resolved.lastIndex) }
    val selected = resolved[selectedIndex.coerceIn(0, resolved.lastIndex)]

    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "%${selected.percentage}",
                style = MaterialTheme.typography.headlineSmall,
                color = lineColor
            )
            Text(
                text = stringResource(R.string.week_of_label, dateFormat.format(Date(selected.weekStartMillis))),
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .pointerInput(resolved) {
                        detectTapGestures { offset ->
                            if (resolved.size < 2) return@detectTapGestures
                            val stepX = size.width / (resolved.size - 1).toFloat()
                            val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, resolved.lastIndex)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val stepX = if (resolved.size > 1) this.size.width / (resolved.size - 1) else 0f

                fun yFor(percentage: Int): Float {
                    return this.size.height * (1f - percentage / 100f)
                }

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

                val onlyPoints = resolved.mapIndexed { index, point ->
                    Offset(index * stepX, yFor(point.percentage!!))
                }
                for (i in 0 until onlyPoints.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = onlyPoints[i],
                        end = onlyPoints[i + 1],
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                onlyPoints.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    drawCircle(
                        color = lineColor,
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

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(resolved.first().weekStartMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    text = dateFormat.format(Date(resolved.last().weekStartMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}
