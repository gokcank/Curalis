package com.gokcank.curalis.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.ReminderState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherenceCalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdherenceCalendarViewModel = hiltViewModel()
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayStatusMap by viewModel.dayStatusMap.collectAsState()
    val selectedDayReminders by viewModel.selectedDayReminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlaç Uyum Takvimi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Month Navigation Header
            MonthNavigationHeader(
                currentYearMonth = currentYearMonth,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Day of Week Header
            DaysOfWeekHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 7-Column Calendar Grid
            CalendarGrid(
                currentYearMonth = currentYearMonth,
                selectedDate = selectedDate,
                dayStatusMap = dayStatusMap,
                onDateSelected = viewModel::onDateSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Selected Day Dosage History Section
            Text(
                text = "📅 ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale("tr"))} ${selectedDate.year} - Doz Kayıtları",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedDayReminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bu tarihe ait kayıtlı ilaç dozu bulunmamaktadır.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedDayReminders) { item ->
                        DailyReminderCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigationHeader(
    currentYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Önceki Ay")
        }

        val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale("tr"))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("tr")) else it.toString() }
        Text(
            text = "$monthName ${currentYearMonth.year}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sonraki Ay")
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentYearMonth: YearMonth,
    selectedDate: LocalDate,
    dayStatusMap: Map<LocalDate, DayAdherenceStatus>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, 1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Mon, 7=Sun
    val paddingDays = firstDayOfWeek - 1
    val totalDays = currentYearMonth.lengthOfMonth()

    val gridItems = mutableListOf<LocalDate?>()
    for (i in 0 until paddingDays) {
        gridItems.add(null)
    }
    for (day in 1..totalDays) {
        gridItems.add(LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, day))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(gridItems) { date ->
            if (date == null) {
                Spacer(modifier = Modifier.aspectRatio(1f))
            } else {
                val status = dayStatusMap[date] ?: DayAdherenceStatus.FUTURE_OR_EMPTY
                val isSelected = date == selectedDate

                val (bgColor, textColor) = when (status) {
                    DayAdherenceStatus.PERFECT -> Color(0xFF2E7D32) to Color.White   // 🟢 Green
                    DayAdherenceStatus.PARTIAL -> Color(0xFFF57F17) to Color.White   // 🟡 Yellow
                    DayAdherenceStatus.MISSED -> Color(0xFFC62828) to Color.White    // 🔴 Red
                    DayAdherenceStatus.FUTURE_OR_EMPTY -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else bgColor,
                            shape = CircleShape
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else textColor
                    )
                }
            }
        }
    }
}

@Composable
fun DailyReminderCard(item: DailyReminderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(item.medicationFormEmoji, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.medicationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.reminder.timeInMillis))
                    Text(
                        text = "⏰ Saat $formattedTime ${item.dosageInfo.takeIf { it.isNotBlank() }?.let { "• $it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val (badgeText, badgeBg, badgeFg) = when (item.reminder.state) {
                ReminderState.TAKEN -> Triple("✅ Alındı", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                ReminderState.SKIPPED -> Triple("❌ Atlandı", Color(0xFFFFF3E0), Color(0xFFE65100))
                ReminderState.MISSED -> Triple("⚠️ Kaçırıldı", Color(0xFFFFEBEE), Color(0xFFC62828))
                ReminderState.SNOOZED -> Triple("⏱️ Ertelendi", Color(0xFFE3F2FD), Color(0xFF1565C0))
                ReminderState.CANCELLED -> Triple("🚫 İptal", Color(0xFFEEEEEE), Color(0xFF757575))
                else -> Triple("🕒 Planlandı", Color(0xFFF5F5F5), Color(0xFF616161))
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
