package com.gokcank.curalis.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.presentation.components.EmptyState
import com.gokcank.curalis.presentation.components.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherenceAnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdherenceAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.adherence_analytics_title), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Uyum Oranı Özet Kartları (Haftalık & Aylık)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdherencePercentageCard(
                            title = stringResource(R.string.weekly_adherence_title),
                            percentage = uiState.weeklyAdherenceRate,
                            subtitle = stringResource(R.string.last_7_days),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary
                        )
                        AdherencePercentageCard(
                            title = stringResource(R.string.monthly_adherence_title),
                            percentage = uiState.monthlyAdherenceRate,
                            subtitle = stringResource(R.string.last_30_days),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // 1.5 Haftalık Uyum Trendi
                if (uiState.weeklyTrend.count { it.percentage != null } >= 2) {
                    item {
                        Text(
                            text = stringResource(R.string.adherence_trend_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    item {
                        AdherenceTrendChart(points = uiState.weeklyTrend)
                    }
                }

                // 2. Haftalık Doz Detay Sayacı
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.weekly_dose_stats_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatBadge(
                                    label = stringResource(R.string.total_dose_label),
                                    value = uiState.totalWeeklyDoses.toString(),
                                    icon = Icons.Default.DateRange,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                StatBadge(
                                    label = stringResource(R.string.taken_label),
                                    value = uiState.takenWeeklyDoses.toString(),
                                    icon = Icons.Default.CheckCircle,
                                    tint = LocalCuralisColors.current.success
                                )
                                StatBadge(
                                    label = stringResource(R.string.not_taken_label),
                                    value = uiState.missedWeeklyDoses.toString(),
                                    // Kaçırılan doz bir uygulama hatası değil; Error rengi
                                    // burada kullanılmaz (design-system.md).
                                    icon = Icons.Default.Warning,
                                    tint = if (uiState.missedWeeklyDoses > 0) {
                                        LocalCuralisColors.current.warning
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. İlaç Bazlı Uyum Dağılımı Başlığı
                item {
                    Text(
                        text = stringResource(R.string.medication_adherence_breakdown_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.medicationStats.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.BarChart,
                            title = stringResource(R.string.no_analytics_data_title),
                            description = stringResource(R.string.no_analytics_data_desc)
                        )
                    }
                } else {
                    items(uiState.medicationStats) { stat ->
                        MedicationStatCard(stat = stat)
                    }
                }
            }
        }
    }
}

@Composable
fun AdherencePercentageCard(
    title: String,
    percentage: Int?,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (percentage == null) {
                // Hiç sonuçlanmış doz yok: boş bir çubuk ve sahte bir yüzde göstermek yerine
                // durumu açıkça söylüyoruz (design-system.md "Empty Visualizations").
                Text(
                    text = "—",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.no_data_yet),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "%$percentage",
                    style = MaterialTheme.typography.headlineLarge,
                    color = color,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
    }
}

@Composable
fun MedicationStatCard(stat: MedicationStat) {
    val fallbackColor = MaterialTheme.colorScheme.primary
    val badgeColor = remember(stat.medication.colorHex, fallbackColor) {
        runCatching { Color(android.graphics.Color.parseColor(stat.medication.colorHex)) }
            .getOrDefault(fallbackColor)
    }
    val semantic = LocalCuralisColors.current
    // Düşük uyum bir hata değil, dikkat çekilmesi gereken bir durumdur.
    val rateColor = when {
        stat.adherencePercentage == null -> MaterialTheme.colorScheme.onSurfaceVariant
        stat.adherencePercentage >= 80 -> semantic.success
        else -> semantic.warning
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = badgeColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = stat.medication.formType.icon(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stat.adherencePercentage?.let { "%$it" } ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = rateColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (stat.adherencePercentage != null) {
                    LinearProgressIndicator(
                        progress = { stat.adherencePercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = rateColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.taken_of_total_doses, stat.takenDoses, stat.totalDoses, stat.missedDoses),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.no_resolved_doses_yet),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
