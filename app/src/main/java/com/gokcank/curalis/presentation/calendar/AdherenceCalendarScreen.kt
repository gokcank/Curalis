package com.gokcank.curalis.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventBusy
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import com.gokcank.curalis.core.theme.LocalCuralisColors
import com.gokcank.curalis.presentation.components.EmptyState
import com.gokcank.curalis.presentation.components.ReminderStateBadge
import com.gokcank.curalis.presentation.components.icon
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
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: AdherenceCalendarViewModel = hiltViewModel()
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayStatusMap by viewModel.dayStatusMap.collectAsState()
    val selectedDayReminders by viewModel.selectedDayReminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.adherence_calendar_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.analytics_content_desc)
                        )
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
            val displayLocale = LocalConfiguration.current.locales[0]
            Text(
                text = stringResource(
                    R.string.selected_day_dose_records_header,
                    selectedDate.dayOfMonth,
                    selectedDate.month.getDisplayName(TextStyle.FULL, displayLocale),
                    selectedDate.year
                ),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedDayReminders.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventBusy,
                    title = stringResource(R.string.no_dose_records_title),
                    description = stringResource(R.string.no_dose_records_desc)
                )
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month_desc))
        }

        val displayLocale = LocalConfiguration.current.locales[0]
        val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, displayLocale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(displayLocale) else it.toString() }
        Text(
            text = stringResource(R.string.calendar_month_year_header, monthName, currentYearMonth.year),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month_desc))
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = androidx.compose.ui.res.stringArrayResource(R.array.weekday_short_names).toList()
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
                val status = dayStatusMap[date] ?: DayAdherenceStatus.FUTURE
                val isSelected = date == selectedDate
                val semantic = LocalCuralisColors.current

                // Renk tek başına bilgi taşımaz (design-system.md "Color Independence"):
                // her durum ayrıca bir işaret ve içerik açıklamasıyla birlikte gelir.
                val (bgColor, textColor) = when (status) {
                    DayAdherenceStatus.PERFECT -> semantic.successContainer to semantic.onSuccessContainer
                    DayAdherenceStatus.PARTIAL -> semantic.warningContainer to semantic.onWarningContainer
                    DayAdherenceStatus.MISSED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                    DayAdherenceStatus.NO_DOSES -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    DayAdherenceStatus.FUTURE -> Color.Transparent to MaterialTheme.colorScheme.onSurfaceVariant
                }

                val statusDescription = when (status) {
                    DayAdherenceStatus.PERFECT -> stringResource(R.string.day_status_all_taken)
                    DayAdherenceStatus.PARTIAL -> stringResource(R.string.day_status_partial)
                    DayAdherenceStatus.MISSED -> stringResource(R.string.day_status_missed)
                    DayAdherenceStatus.NO_DOSES -> stringResource(R.string.day_status_no_doses)
                    DayAdherenceStatus.FUTURE -> stringResource(R.string.day_status_future)
                }
                val dayContentDescription = stringResource(R.string.calendar_day_content_desc, date.dayOfMonth, statusDescription)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else bgColor,
                            shape = CircleShape
                        )
                        .then(
                            if (status == DayAdherenceStatus.FUTURE && !isSelected) {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            } else Modifier
                        )
                        .clickable { onDateSelected(date) }
                        .semantics {
                            contentDescription = dayContentDescription
                        },
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.medicationForm.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.medicationName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                    val formattedTime = timeFormat.format(Date(item.reminder.timeInMillis))
                    val dosageSuffix = item.dosageInfo.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""
                    Text(
                        text = "Saat $formattedTime$dosageSuffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            ReminderStateBadge(state = item.reminder.state)
        }
    }
}
