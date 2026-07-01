package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import android.provider.Settings
import android.os.Build
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HydrateViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

@Composable
fun HomeScreen(viewModel: HydrateViewModel, onNavigateToControl: () -> Unit) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val todayIntake by viewModel.todayIntake.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val cupSize by viewModel.cupSize.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val displayOverlayScreen by viewModel.displayOverlayScreen.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else {
                    true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftWhite)
    ) {
        // Geometric Balance Top Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Brush.verticalGradient(listOf(RoyalBlue, DeepOceanBlue)))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = CyanAccent.copy(alpha = 0.2f), radius = 300f, center = Offset(size.width, -100f))
                drawCircle(color = AquaBlue.copy(alpha = 0.1f), radius = 400f, center = Offset(-100f, 400f))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
        ) {
            item {
                TopGreetingSection(name, onNavigateToControl)
            }
            if (remindersEnabled && displayOverlayScreen && !hasOverlayPermission) {
                item {
                    PermissionBannerSection(
                        onGrant = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
            item {
                WaterProgressSection(
                    current = todayIntake,
                    goal = dailyGoal,
                    onAddSingle = { viewModel.addWater(cupSize) }
                )
            }
            item {
                QuickAddSection(onAdd = { viewModel.addWater(it) })
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Today's Log",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepOceanBlue
                    )
                    Text(
                        "VIEW MORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(todayLogs) { log ->
                HistoryCard(log.amountMl, log.timeInMillis)
            }
            if (todayLogs.isEmpty()) {
                item {
                    Text(
                        "No water intake yet today.",
                        color = DeepOceanBlue.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TopGreetingSection(name: String, onNavigateToControl: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good Morning", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SoftWhite.copy(alpha = 0.7f))
            Text("$name 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SoftWhite)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToControl() }
                    .border(1.dp, SoftWhite.copy(alpha = 0.3f), CircleShape)
                    .background(GlassWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 20.sp)
                Box(modifier = Modifier.size(8.dp).background(CyanAccent, CircleShape).align(Alignment.TopEnd).offset((-6).dp, 6.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToControl() }
                    .border(2.dp, CyanAccent, CircleShape)
                    .background(SoftWhite),
                contentAlignment = Alignment.Center
            ) {
                Text("👦", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun WaterProgressSection(current: Int, goal: Int, onAddSingle: () -> Unit) {
    val percentage = (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    
    val infiniteTransition = rememberInfiniteTransition(label = "water_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "wave"
    )

    val animatedFill by animateFloatAsState(targetValue = percentage, animationSpec = tween(1500), label = "fill")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .shadow(40.dp, CircleShape, spotColor = CyanAccent)
                .background(GlassWhite, CircleShape)
                .border(1.dp, SoftWhite.copy(alpha = 0.2f), CircleShape)
                .padding(16.dp)
                .border(4.dp, GlassWhite, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val waveHeight = 20f
                val fillHeight = height - (animatedFill * height)
                
                if (animatedFill > 0f) {
                    val path = Path().apply {
                        moveTo(0f, fillHeight)
                        for (x in 0..width.toInt() step 5) {
                            val y = sin((x / width * 2 * Math.PI) + waveOffset.toDouble()) * waveHeight + fillHeight
                            lineTo(x.toFloat(), y.toFloat())
                        }
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(path, brush = Brush.verticalGradient(listOf(CyanAccent, AquaBlue.copy(alpha = 0.8f))), style = Fill)
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${(animatedFill * 100).toInt()}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftWhite
                    )
                    Text(
                        "%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = SoftWhite,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    "$current / ${goal} ml",
                    fontSize = 14.sp,
                    color = SoftWhite.copy(alpha = 0.7f)
                )
            }
        }
        
        // Floating action button on top edge of circle
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp)
                .size(64.dp)
                .border(4.dp, SoftWhite, CircleShape)
                .shadow(16.dp, CircleShape)
                .background(Color.White, CircleShape)
                .clickable { onAddSingle() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 32.sp, color = RoyalBlue, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun QuickAddSection(onAdd: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(100, 150, 200).forEach { amount ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RoyalBlue.copy(alpha = 0.12f))
                    .border(1.dp, RoyalBlue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onAdd(amount) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+$amount", color = RoyalBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("ml", color = RoyalBlue.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(amount: Int, timestamp: Long) {
    val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AquaBlue.copy(alpha=0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Water Log", color = DeepOceanBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(timeString, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
        Text("+$amount ml", color = RoyalBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun PermissionBannerSection(onGrant: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Warning icon",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overlay Permission Required",
                    color = Color(0xFF991B1B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "To show the water break overlay automatically, please enable 'Draw over other apps' in settings.",
                    color = Color(0xFF7F1D1D),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Grant Permission", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
