package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.HydrateViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
fun ProgressScreen(viewModel: HydrateViewModel, navController: NavController) {
    val darkTheme = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val todayIntake by viewModel.todayIntake.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Interactive Dialog state for "Add Water"
    var showAddWaterDialog by remember { mutableStateOf(false) }

    // State for Time Filter Tabs
    var selectedTab by remember { mutableStateOf("Day") }

    // State for Tapped Bar index (-1 means none)
    var selectedBarIndex by remember { mutableStateOf(-1) }

    // Clean white theme colors
    val backgroundColor = Color.White
    val cardBackgroundColor = Color.White
    val textPrimaryColor = Color(0xFF202124)
    val textSecondaryColor = Color(0xFF7A7A7A)
    val borderColor = Color(0xFFEAF2FA)

    // Animated entry for cards
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Back Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, borderColor, CircleShape)
                        .clickable { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = textPrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title
                Text(
                    text = "Progress",
                    color = textPrimaryColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Rounded Add Button - Filled style for a vibrant high-contrast action
                Button(
                    onClick = { showAddWaterDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Intake summary and Date section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Water Drop Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💧", fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = "$todayIntake ml",
                            color = textPrimaryColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Today",
                            color = textSecondaryColor,
                            fontSize = 14.sp
                        )
                    }
                }

                // Date Box
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar Icon",
                        tint = textSecondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    val currentDateStr = remember {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    }
                    Text(
                        text = currentDateStr,
                        color = textPrimaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Removed the filter container as requested, replaced with a small spacer
            Spacer(modifier = Modifier.height(8.dp))

            // Animations and Slide Up Content
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(400)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // CARD 1: Bar Chart
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = Color(0xFF2196F3).copy(alpha = 0.08f),
                                ambientColor = Color(0xFF2196F3).copy(alpha = 0.08f)
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Day chart",
                                color = textPrimaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom Bar Chart Render
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            ) {
                                // Calculate dynamic values for Mon-Sun week logs
                                val weekIntakes = remember(allLogs) {
                                    val result = FloatArray(7)
                                    val cal = Calendar.getInstance()
                                    val currentYear = cal.get(Calendar.YEAR)
                                    val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)

                                    allLogs.forEach { log ->
                                        val logCal = Calendar.getInstance().apply { timeInMillis = log.timeInMillis }
                                        if (logCal.get(Calendar.YEAR) == currentYear && logCal.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                                            val dayOfWeek = logCal.get(Calendar.DAY_OF_WEEK)
                                            val index = when (dayOfWeek) {
                                                Calendar.MONDAY -> 0
                                                Calendar.TUESDAY -> 1
                                                Calendar.WEDNESDAY -> 2
                                                Calendar.THURSDAY -> 3
                                                Calendar.FRIDAY -> 4
                                                Calendar.SATURDAY -> 5
                                                Calendar.SUNDAY -> 6
                                                else -> 0
                                            }
                                            result[index] += log.amountMl.toFloat()
                                        }
                                    }
                                    result
                                }

                                // Interactive Click Handler overlaid on Canvas
                                val weekdays = listOf("Mon", "Tues", "Wed", "Thurs", "Fri", "Sat", "Sun")
                                val currentDayIndex = remember {
                                    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                                    when (day) {
                                        Calendar.MONDAY -> 0
                                        Calendar.TUESDAY -> 1
                                        Calendar.WEDNESDAY -> 2
                                        Calendar.THURSDAY -> 3
                                        Calendar.FRIDAY -> 4
                                        Calendar.SATURDAY -> 5
                                        Calendar.SUNDAY -> 6
                                        else -> 0
                                    }
                                }

                                // Growth animation for bars
                                val animationProgress = remember { Animatable(0f) }
                                LaunchedEffect(selectedTab) {
                                    animationProgress.snapTo(0f)
                                    animationProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
                                }

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            // Optional click to clear
                                            selectedBarIndex = -1
                                        }
                                ) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height

                                    // Chart dimensions leaving padding for labels
                                    val chartLeftPadding = 70f
                                    val chartBottomPadding = 60f
                                    val chartRightPadding = 20f
                                    val chartTopPadding = 40f

                                    val graphWidth = canvasWidth - chartLeftPadding - chartRightPadding
                                    val graphHeight = canvasHeight - chartTopPadding - chartBottomPadding

                                    // Y Axis Grid lines (0 to 2500 ml / 2.5L)
                                    val maxVal = 2500f
                                    val yLines = listOf(0f, 500f, 1000f, 1500f, 2000f, 2500f)

                                    yLines.forEach { yValue ->
                                        val yRatio = yValue / maxVal
                                        val yPos = chartTopPadding + graphHeight * (1f - yRatio)

                                        // Draw Grid Line
                                        drawLine(
                                            color = borderColor.copy(alpha = 0.5f),
                                            start = Offset(chartLeftPadding, yPos),
                                            end = Offset(canvasWidth - chartRightPadding, yPos),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )

                                        // Draw Label text
                                        val labelText = when (yValue) {
                                            2500f -> "2.5L"
                                            2000f -> "2L"
                                            1500f -> "1.5L"
                                            1000f -> "1L"
                                            500f -> "500"
                                            else -> "0"
                                        }
                                        drawText(
                                            textMeasurer = textMeasurer,
                                            text = labelText,
                                            style = androidx.compose.ui.text.TextStyle(
                                                color = textSecondaryColor,
                                                fontSize = 11.sp
                                            ),
                                            topLeft = Offset(20f, yPos - 14f)
                                        )
                                    }

                                    // Draw Bars
                                    val barCount = 7
                                    val barWidth = 32.dp.toPx()
                                    val spacing = (graphWidth - (barWidth * barCount)) / (barCount + 1)

                                    for (i in 0 until barCount) {
                                        val intake = weekIntakes[i]
                                        val intakeRatio = (intake / maxVal).coerceAtMost(1f)
                                        val rawBarHeight = graphHeight * intakeRatio
                                        val animatedBarHeight = rawBarHeight * animationProgress.value

                                        val barLeft = chartLeftPadding + spacing + i * (barWidth + spacing)
                                        val barTop = chartTopPadding + graphHeight - animatedBarHeight

                                        // Check if this is the active (or selected) bar
                                        val isCurrentDay = i == currentDayIndex
                                        val isSelected = i == selectedBarIndex

                                        val brush = if (isSelected || isCurrentDay) {
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFF42A5F5), Color(0xFF2196F3))
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFF90CAF9).copy(alpha = 0.3f), Color(0xFF2196F3).copy(alpha = 0.2f))
                                            )
                                        }

                                        // Draw the Bar
                                        drawRoundRect(
                                            brush = brush,
                                            topLeft = Offset(barLeft, barTop),
                                            size = Size(barWidth, animatedBarHeight.coerceAtLeast(4f)),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )

                                        // Draw X Axis labels (Mon, Tues, ...)
                                        val textLayoutResult = textMeasurer.measure(
                                            text = weekdays[i],
                                            style = androidx.compose.ui.text.TextStyle(
                                                color = if (isCurrentDay) Color(0xFF2196F3) else textSecondaryColor,
                                                fontSize = 11.sp,
                                                fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                        drawText(
                                            textLayoutResult = textLayoutResult,
                                            topLeft = Offset(
                                                x = barLeft + (barWidth - textLayoutResult.size.width) / 2f,
                                                y = canvasHeight - 35f
                                            )
                                        )
                                    }
                                }

                                // Interactive Click overlays for each bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 55.dp, end = 15.dp, bottom = 45.dp)
                                        .fillMaxHeight()
                                ) {
                                    for (i in 0..6) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    selectedBarIndex = if (selectedBarIndex == i) -1 else i
                                                }
                                        )
                                    }
                                }

                                // Tooltip Popup over selected bar
                                if (selectedBarIndex != -1) {
                                    val intake = weekIntakes[selectedBarIndex].toInt()
                                    val dayName = weekdays[selectedBarIndex]

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .offset(y = (-10).dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$dayName: $intake ml",
                                            color = textPrimaryColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // CARD 2: Hydration Streak / Day Streak
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = Color(0xFF2196F3).copy(alpha = 0.08f),
                                ambientColor = Color(0xFF2196F3).copy(alpha = 0.08f)
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2196F3).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Calendar",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Day Streak",
                                    color = textPrimaryColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Streaks Mon-Sun row
                            val weekDaysLetters = listOf("Mon", "Tues", "Wed", "Thurs", "Fri", "Sat", "Sun")
                            val weekIntakes = remember(allLogs) {
                                val result = FloatArray(7)
                                val cal = Calendar.getInstance()
                                val currentYear = cal.get(Calendar.YEAR)
                                val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)

                                allLogs.forEach { log ->
                                        val logCal = Calendar.getInstance().apply { timeInMillis = log.timeInMillis }
                                        if (logCal.get(Calendar.YEAR) == currentYear && logCal.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                                            val dayOfWeek = logCal.get(Calendar.DAY_OF_WEEK)
                                            val index = when (dayOfWeek) {
                                                Calendar.MONDAY -> 0
                                                Calendar.TUESDAY -> 1
                                                Calendar.WEDNESDAY -> 2
                                                Calendar.THURSDAY -> 3
                                                Calendar.FRIDAY -> 4
                                                Calendar.SATURDAY -> 5
                                                Calendar.SUNDAY -> 6
                                                else -> 0
                                            }
                                            result[index] += log.amountMl.toFloat()
                                        }
                                    }
                                result
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (i in 0..6) {
                                    val isCompleted = weekIntakes[i] >= dailyGoal
                                    val isUpcoming = i > Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }.get(Calendar.DAY_OF_WEEK) - 2

                                    // Animation scale for completed checkmarks
                                    val scaleAnim by animateFloatAsState(
                                        targetValue = if (isCompleted) 1.1f else 1.0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = weekDaysLetters[i],
                                            fontSize = 11.sp,
                                            color = textSecondaryColor,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .scale(scaleAnim)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isCompleted) Color(0xFF2196F3) else Color.White
                                                )
                                                .border(1.dp, borderColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF2196F3).copy(alpha = 0.3f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dynamic streak message based on actual completed days
                            val completedCount = weekIntakes.count { it >= dailyGoal }
                            val streakMessage = when {
                                completedCount == 7 -> "🔥 Amazing streak! keep going everyday champ"
                                completedCount > 3 -> "🎉 Great progress! You're staying hydrated!"
                                completedCount > 0 -> "💙 Doing great! Drink water to lock your streak."
                                else -> "💧 Start tracking your daily hydration to build a streak!"
                            }

                            Text(
                                text = streakMessage,
                                color = if (completedCount > 0) Color(0xFF2196F3) else textSecondaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }

                    // CARD 3: Water Intake Timeline
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = Color(0xFF2196F3).copy(alpha = 0.08f),
                                ambientColor = Color(0xFF2196F3).copy(alpha = 0.08f)
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Top Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2196F3).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Clock",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "Time of the day",
                                        color = textPrimaryColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2196F3))
                                    )
                                    Text(
                                        text = "Healthy",
                                        color = textSecondaryColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "$todayIntake ml",
                                color = textPrimaryColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Horizontal Timeline capsules (6 AM to 8 PM)
                            val intervals = listOf(
                                "6 AM", "8 AM", "10 AM", "12 PM", "2 PM", "4 PM", "6 PM", "8 PM"
                            )

                            // Find which interval contains current time
                            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            val activeIntervalIndex = when {
                                currentHour <= 6 -> 0
                                currentHour <= 8 -> 1
                                currentHour <= 10 -> 2
                                currentHour <= 12 -> 3
                                currentHour <= 14 -> 4
                                currentHour <= 16 -> 5
                                currentHour <= 18 -> 6
                                else -> 7
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        intervals.forEachIndexed { index, label ->
                                            val isActive = index == activeIntervalIndex
                                            val isPast = index < activeIntervalIndex
                                            val isUpcoming = index > activeIntervalIndex

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                // Small triangle indicator above active capsule
                                                Box(
                                                    modifier = Modifier.height(8.dp),
                                                    contentAlignment = Alignment.BottomCenter
                                                ) {
                                                    if (isActive) {
                                                        // Simple triangle using custom canvas
                                                        Canvas(modifier = Modifier.size(8.dp)) {
                                                            val path = Path().apply {
                                                                moveTo(size.width / 2f, size.height)
                                                                lineTo(0f, 0f)
                                                                lineTo(size.width, 0f)
                                                                close()
                                                            }
                                                            drawPath(path = path, color = textPrimaryColor)
                                                        }
                                                    }
                                                }

                                                // Capsule background
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.85f)
                                                        .height(24.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .border(1.dp, if (isActive || isPast) Color.Transparent else borderColor, RoundedCornerShape(6.dp))
                                                        .background(
                                                            when {
                                                                isActive -> Color(0xFF2196F3)
                                                                isPast -> Color(0xFF2196F3).copy(alpha = 0.5f)
                                                                else -> Color.White
                                                            }
                                                        )
                                                )

                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    color = textSecondaryColor,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Dialog: Add Water
    if (showAddWaterDialog) {
        AlertDialog(
            onDismissRequest = { showAddWaterDialog = false },
            title = {
                Text(
                    text = "Log Water Intake",
                    color = textPrimaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick add presets:",
                        color = textSecondaryColor,
                        fontSize = 14.sp
                    )

                    val presets = listOf(150, 250, 350, 500)
                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                Button(
                                    onClick = {
                                        viewModel.addWater(preset)
                                        showAddWaterDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3).copy(alpha = 0.12f),
                                        contentColor = Color(0xFF2196F3)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("$preset ml", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddWaterDialog = false }) {
                    Text("Close", color = Color.Black)
                }
            },
            containerColor = cardBackgroundColor,
            shape = RoundedCornerShape(26.dp)
        )
    }
}
