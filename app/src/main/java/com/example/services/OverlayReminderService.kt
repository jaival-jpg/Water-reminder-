package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import android.app.Service
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.net.Uri
import com.example.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.data.WaterLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.sin

class OverlayReminderService : Service(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var mediaPlayer: MediaPlayer? = null

    // Required owners to run Jetpack Compose smoothly inside WindowManager
    private val viewModelStore = ViewModelStore()
    private val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = this@OverlayReminderService.viewModelStore
    }

    private val savedStateRegistryOwner = object : SavedStateRegistryOwner {
        private val controller = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = this@OverlayReminderService.lifecycle
        override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry

        init {
            controller.performRestore(null)
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        showFloatingOverlay()
        startRingtone()
        return START_NOT_STICKY
    }

    private fun startRingtone() {
        try {
            stopRingtone() // clean up any existing instance
            val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.water_alarm)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = false
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "water_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Water Reminder Active Alarm",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            99,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Water Alarm")
            .setContentText("Overlay reminder active")
            .setSmallIcon(R.drawable.ic_notification_drop)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(999, notification)
    }

    private fun showFloatingOverlay() {
        if (composeView != null) return // Already showing

        val width = (resources.displayMetrics.widthPixels * 0.92).toInt()
        val params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayReminderService)
            setViewTreeViewModelStoreOwner(viewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)

            setContent {
                OverlayCardContent(
                    onDismiss = { dismissOverlay() },
                    onAddWaterDirect = { amount -> addWaterIntake(amount) }
                )
            }
        }

        try {
            windowManager?.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addWaterIntake(amountMl: Int) {
        serviceScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(applicationContext)
            database.waterLogDao().insertLog(
                WaterLog(amountMl = amountMl, timeInMillis = System.currentTimeMillis())
            )
        }
    }

    private fun dismissOverlay() {
        stopRingtone()
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        composeView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeView = null
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): android.os.IBinder? {
        return null
    }

    override fun onDestroy() {
        stopRingtone()
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
        composeView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelStore.clear()
        super.onDestroy()
    }
}

@Composable
fun OverlayCardContent(
    onDismiss: () -> Unit,
    onAddWaterDirect: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Real-time flows directly from the repository and database
    val settingsRepository = remember { SettingsRepository(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    val waterLogDao = remember { database.waterLogDao() }

    val dailyGoal = remember { mutableStateOf(2000) }
    val todayIntake = remember { mutableStateOf(0) }

    // Start & end of today logic to match view model
    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val endOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    LaunchedEffect(Unit) {
        launch {
            settingsRepository.dailyGoalFlow.collect { goal ->
                dailyGoal.value = goal
            }
        }
        launch {
            waterLogDao.getLogsBetween(startOfToday, endOfToday).collect { logs ->
                todayIntake.value = logs.sumOf { it.amountMl }
            }
        }
    }

    // Selected preset amount to use for "Drink Now"
    var selectedAmount by remember { mutableStateOf(150) }

    // Visibility state for entering/exiting animations
    var isEnteringVisible by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isEnteringVisible = true
    }

    // Scale and alpha animations
    val scale by animateFloatAsState(
        targetValue = if (isEnteringVisible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isEnteringVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    fun startDismissSequence() {
        isEnteringVisible = false
        coroutineScope.launch {
            delay(300)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
            .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // TOP BAR
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Left Icon + Title Column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Animated Water Drop Icon
                    val dropScaleInfinite = rememberInfiniteTransition()
                    val dropScaleFactor by dropScaleInfinite.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer(scaleX = dropScaleFactor, scaleY = dropScaleFactor)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💧", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Time to Drink Water 💧",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E258C)
                        )
                        Text(
                            text = "Stay hydrated and keep healthy.",
                            fontSize = 12.sp,
                            color = Color(0xFF8E9CB2)
                        )
                    }
                }

                // Top Right Close Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { startDismissSequence() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF8E9CB2),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // MAIN SECTION (Water Drop representation)
            // ==========================================
            val animatedPercent by animateFloatAsState(
                targetValue = if (dailyGoal.value > 0) {
                    (todayIntake.value.toFloat() / dailyGoal.value.toFloat()).coerceIn(0f, 1f)
                } else 0f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )

            // Wave phase shift infinite transition
            val phaseShiftInfinite = rememberInfiniteTransition()
            val phaseShift by phaseShiftInfinite.animateFloat(
                initialValue = 0f,
                targetValue = (2 * Math.PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val widthPx = size.width
                    val heightPx = size.height

                    // Mathematical Teardrop Path
                    val dropPath = Path().apply {
                        moveTo(widthPx / 2f, 0f)
                        cubicTo(
                            widthPx * 0.92f, heightPx * 0.38f,
                            widthPx, heightPx,
                            widthPx / 2f, heightPx
                        )
                        cubicTo(
                            0f, heightPx,
                            widthPx * 0.08f, heightPx * 0.38f,
                            widthPx / 2f, 0f
                        )
                        close()
                    }

                    // Clip to drawing inside teardrop only
                    clipPath(dropPath) {
                        // Light background color of teardrop
                        drawPath(dropPath, color = Color(0xFF2196F3).copy(alpha = 0.08f))

                        // Dynamic Wave Height Calculation
                        val waterLevelY = heightPx - (heightPx * animatedPercent)

                        val wavePath = Path().apply {
                            moveTo(0f, heightPx)
                            val waveAmplitude = 10.dp.toPx()
                            val waveFrequency = 1.35f * Math.PI.toFloat() / widthPx

                            for (x in 0..widthPx.toInt()) {
                                val y = waterLevelY + waveAmplitude * sin(waveFrequency * x + phaseShift)
                                lineTo(x.toFloat(), y)
                            }
                            lineTo(widthPx, heightPx)
                            close()
                        }

                        drawPath(
                            path = wavePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF42A5F5), Color(0xFF0E258C))
                            )
                        )
                    }

                    // Stroke border for crisp shape definition
                    drawPath(
                        path = dropPath,
                        color = Color(0xFF2196F3).copy(alpha = 0.3f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }

                // Stats inside the Teardrop Container
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val displayPct = (animatedPercent * 100).toInt()
                    Text(
                        text = "$displayPct%",
                        color = if (animatedPercent > 0.45f) Color.White else Color(0xFF0E258C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${todayIntake.value} / ${dailyGoal.value} ml",
                        color = if (animatedPercent > 0.45f) Color.White.copy(alpha = 0.85f) else Color(0xFF0E258C).copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // MESSAGE CARD
            // ==========================================
            val remainingMl = (dailyGoal.value - todayIntake.value).coerceAtLeast(0)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2196F3).copy(alpha = 0.08f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💙", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (remainingMl > 0) {
                        "You are only ${remainingMl} ml away from today's goal. Keep drinking water regularly."
                    } else {
                        "Amazing work! You've reached today's hydration goal! 🎉"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF0E258C),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // QUICK ADD BUTTONS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(100, 150, 200).forEach { amount ->
                    val isSelected = selectedAmount == amount
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) Color(0xFF0E258C)
                                else Color(0xFF0E258C).copy(alpha = 0.08f)
                            )
                            .clickable {
                                selectedAmount = amount
                                // Tapping Quick Add instantly increases water intake, saves locally, keeps overlay open
                                onAddWaterDirect(amount)
                            }
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = if (isSelected) Color.Transparent else Color(0xFF0E258C).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$amount ml",
                            color = if (isSelected) Color.White else Color(0xFF0E258C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // BOTTOM ACTIONS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT: Skip Button
                Button(
                    onClick = { startDismissSequence() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0E258C)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .border(1.dp, Color(0xFF0E258C).copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Skip",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // RIGHT: Drink Now Button
                Button(
                    onClick = {
                        // Play success animation, update progress, close automatically after 800ms
                        showSuccessAnimation = true
                        onAddWaterDirect(selectedAmount)
                        coroutineScope.launch {
                            delay(800)
                            startDismissSequence()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0E258C),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Drink Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Success overlay triggered by Drink Now
        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val scaleAnim = remember { Animatable(0.5f) }
                    LaunchedEffect(Unit) {
                        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer(scaleX = scaleAnim.value, scaleY = scaleAnim.value)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Intake Logged! 💧",
                        color = Color(0xFF0E258C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+$selectedAmount ml successfully recorded",
                        color = Color(0xFF8E9CB2),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
