package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CpuWakeEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuMonitorScreen(
    viewModel: CpuMonitorViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToSafeDirectory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeframeHours by viewModel.timeframeHours.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text("Power Telemetry", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "cpu",
                onNavigateToHome = onNavigateToHome,
                onNavigateToProfiles = onNavigateToProfiles,
                onNavigateToCpu = {},
                onNavigateToSafeDirectory = onNavigateToSafeDirectory
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // M3 Segmented Timeframe Choice
            TimeframeChooserRow(
                currentHours = timeframeHours,
                onSelected = { viewModel.setTimeframe(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )

            when (uiState) {
                is CpuMonitorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CpuMonitorUiState.Success -> {
                    val state = uiState as CpuMonitorUiState.Success
                    val events = state.events
                    val topOffender = state.topOffenderPackage

                    // Displays line chart
                    InteractiveLineChartCard(
                        events = events,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Top offender option
                    if (topOffender != null) {
                        TopOffenderSummaryBanner(
                            packageName = topOffender,
                            onFreeze = {
                                viewModel.freezeTopOffender(topOffender) { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recent Wakers Logs:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    // Logs Table
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(events) { event ->
                            CpuWakeLogItem(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeframeChooserRow(
    currentHours: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            8 to "Last 8h",
            24 to "24 Hours",
            168 to "7 Days"
        )
        options.forEach { (hours, label) ->
            val isSelected = currentHours == hours
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(hours) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun InteractiveLineChartCard(
    events: List<CpuWakeEvent>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw horizontal coordinate grid lines
                val gridColor = Color.Gray.copy(alpha = 0.15f)
                drawLine(gridColor, Offset(0f, height * 0.25f), Offset(width, height * 0.25f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, height * 0.75f), Offset(width, height * 0.75f), strokeWidth = 1f)

                if (events.isEmpty()) return@Canvas

                val sortedEvents = events.sortedBy { it.timestamp }
                val rawMax = events.maxOfOrNull { it.wakeCount }?.toFloat() ?: 10f
                val maxWakeCount = if (rawMax == 0f) 10f else rawMax
                val pointsCount = sortedEvents.size

                val path = Path()
                val fillPath = Path()

                val stepX = if (pointsCount > 1) width / (pointsCount - 1) else width
                
                sortedEvents.forEachIndexed { idx, event ->
                    val rawRatioValue = event.wakeCount.toFloat() / maxWakeCount
                    val ratioY = if (rawRatioValue.isNaN() || rawRatioValue.isInfinite()) 0.5f else rawRatioValue.coerceIn(0.1f, 0.9f)
                    val x = idx * stepX
                    val y = height - (ratioY * height)

                    if (idx == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        // Drawing smoothed peak connectors
                        val rawPrevRatio = sortedEvents[idx - 1].wakeCount.toFloat() / maxWakeCount
                        val prevRatioY = if (rawPrevRatio.isNaN() || rawPrevRatio.isInfinite()) 0.5f else rawPrevRatio.coerceIn(0.1f, 0.9f)
                        val prevX = (idx - 1) * stepX
                        val prevY = height - (prevRatioY * height)
                        
                        path.cubicTo(
                            prevX + (stepX / 2), prevY,
                            x - (stepX / 2), y,
                            x, y
                        )
                        fillPath.cubicTo(
                            prevX + (stepX / 2), prevY,
                            x - (stepX / 2), y,
                            x, y
                        )
                    }

                    if (idx == pointsCount - 1) {
                        fillPath.lineTo(x, height)
                        fillPath.close()
                    }
                }

                // Render the background gradient below the telemetry crest
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3).copy(alpha = 0.35f),
                        Color(0xFF2196F3).copy(alpha = 0.0f)
                    )
                )
                drawPath(fillPath, brush = brush)

                // Render the neon telemetry crest outline
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = 4f)
                )
            }
            
            Text(
                "CPU Wakeups Profile",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
fun TopOffenderSummaryBanner(
    packageName: String,
    onFreeze: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Top Resource Offender",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
            Button(
                onClick = onFreeze,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Freeze", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun CpuWakeLogItem(event: CpuWakeEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${event.wakeCount}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.packageName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "Active Duration: ${event.durationMs / 1000f}s | Delta: ${String.format("%.2f", event.batteryDelta)}%",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
