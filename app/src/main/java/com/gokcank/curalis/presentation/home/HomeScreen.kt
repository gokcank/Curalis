package com.gokcank.curalis.presentation.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gokcank.curalis.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMedications: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToVitals: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.app_name), 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = stringResource(R.string.about),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

            // Programmatic Vector Medical Background (100% Noise-Free)
            val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.15f else 0.08f)

            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height

                // Soft Ambient Radial Gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            lineColor.copy(alpha = if (isDark) 0.12f else 0.06f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.35f),
                        radius = width * 0.8f
                    )
                )

                // ECG Pulse Wave Path 1
                val ecgPath1 = Path().apply {
                    moveTo(0f, height * 0.38f)
                    lineTo(width * 0.2f, height * 0.38f)
                    lineTo(width * 0.25f, height * 0.34f)
                    lineTo(width * 0.3f, height * 0.42f)
                    lineTo(width * 0.35f, height * 0.26f) // Peak
                    lineTo(width * 0.4f, height * 0.44f)
                    lineTo(width * 0.44f, height * 0.38f)
                    lineTo(width, height * 0.38f)
                }
                drawPath(
                    path = ecgPath1,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // ECG Pulse Wave Path 2 (Lower)
                val ecgPath2 = Path().apply {
                    moveTo(0f, height * 0.72f)
                    lineTo(width * 0.55f, height * 0.72f)
                    lineTo(width * 0.6f, height * 0.69f)
                    lineTo(width * 0.64f, height * 0.75f)
                    lineTo(width * 0.68f, height * 0.61f) // Peak
                    lineTo(width * 0.73f, height * 0.76f)
                    lineTo(width * 0.77f, height * 0.72f)
                    lineTo(width, height * 0.72f)
                }
                drawPath(
                    path = ecgPath2,
                    color = lineColor.copy(alpha = if (isDark) 0.1f else 0.05f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Decorative Medical Molecules / Circles
                drawCircle(
                    color = lineColor,
                    radius = 40.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.82f, height * 0.22f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = lineColor,
                    radius = 20.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.82f, height * 0.22f)
                )
                drawCircle(
                    color = lineColor.copy(alpha = 0.5f),
                    radius = 16.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.6f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Dynamic Greeting
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 5..11 -> stringResource(R.string.greeting_morning)
                    in 12..17 -> stringResource(R.string.greeting_day)
                    in 18..22 -> stringResource(R.string.greeting_evening)
                    else -> stringResource(R.string.greeting_night)
                }
                
                val dateFormat = SimpleDateFormat("d MMMM EEEE", java.util.Locale.getDefault())
                val dateString = dateFormat.format(Date())

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Quick Glance Cards
                if (uiState.nextMedication != null || uiState.nextAppointment != null || uiState.latestVital != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.nextMedication?.let { (reminder, med) ->
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(reminder.timeInMillis))
                            SummaryCard(
                                title = stringResource(R.string.next_medication),
                                content = "${med?.name ?: stringResource(R.string.unknown)} - $timeStr",
                                icon = Icons.AutoMirrored.Outlined.List
                            )
                        }
                        
                        if (uiState.nextMedication == null) {
                            uiState.nextAppointment?.let { appt ->
                                val dateStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(appt.timeInMillis))
                                SummaryCard(
                                    title = stringResource(R.string.upcoming_appointment),
                                    content = "${appt.title} - $dateStr",
                                    icon = Icons.Default.DateRange
                                )
                            }
                        }
                        
                        // Daily Progress
                        if (uiState.dailyProgress > 0f || uiState.nextMedication != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.daily_progress), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Text(text = stringResource(R.string.medication_tracking), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = { uiState.dailyProgress },
                                            modifier = Modifier.size(48.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            strokeWidth = 6.dp
                                        )
                                        Text(
                                            text = "${(uiState.dailyProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_medications),
                        icon = Icons.AutoMirrored.Outlined.List,
                        onClick = onNavigateToMedications
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_doctors),
                        icon = Icons.Default.Person,
                        onClick = onNavigateToDoctors
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_appointments),
                        icon = Icons.Default.DateRange,
                        onClick = onNavigateToAppointments
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_title_vitals),
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToVitals
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, content: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = content, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
