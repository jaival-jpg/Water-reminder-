package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.R
import com.example.ui.HydrateViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen(navController: NavController, viewModel: HydrateViewModel) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigation and state variables
    var showPages by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) } // Steps 1, 2, 3

    // State for tracking permission status
    var hasNotificationPermission by remember {
        mutableStateOf(hasNotificationPermissionCheck(context))
    }
    var hasOverlayPermission by remember {
        mutableStateOf(hasOverlayPermissionCheck(context))
    }

    // Refresh permission statuses periodically on step changes or app resume
    LaunchedEffect(currentStep) {
        while (true) {
            hasNotificationPermission = hasNotificationPermissionCheck(context)
            hasOverlayPermission = hasOverlayPermissionCheck(context)
            delay(500) // Poll to catch immediate settings updates
        }
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            currentStep = 3 // Move to next screen automatically once permission is granted
        }
    }

    LaunchedEffect(hasCompletedOnboarding) {
        if (hasCompletedOnboarding != null) {
            if (hasCompletedOnboarding == true) {
                // If they have already completed everything, direct to main home screen
                navController.navigate("home") { popUpTo("splash") { inclusive = true } }
            } else {
                // Show onboarding splash pages
                showPages = true
            }
        }
    }

    if (!showPages) {
        // Simple elegant initial branding splash loading state while reading DataStore
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(16.dp, CircleShape, spotColor = CyanAccent)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_droplet_icon_1782538906579),
                        contentDescription = "Water Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Hydrate",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepOceanBlue
                )
            }
        }
    } else {
        // Elegant 3-page Onboarding Splash Screen Flow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Content Area with transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                        },
                        label = "splash_pages"
                    ) { step ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Large stylized illustration based on the active step
                            val (imageRes, title, description) = when (step) {
                                1 -> Triple(
                                    R.drawable.img_onboarding_water_intro_1782744285451,
                                    "Make it simple",
                                    "Drinking enough water is essential for your body. Monitor and track your daily water intake easily to maintain a healthy lifestyle."
                                )
                                2 -> Triple(
                                    R.drawable.img_onboarding_notifications_1782744301375,
                                    "Stay notified",
                                    "To help you build a consistent habit, allow us to send timely and friendly water reminders. Let's keep you hydrated!"
                                )
                                else -> Triple(
                                    R.drawable.img_onboarding_overlay_1782744315767,
                                    "Zero reminders missed",
                                    "Allow overlay screen permissions to show urgent hydration prompts over other apps, ensuring you never miss a critical reminder."
                                )
                            }

                            // Big visual illustration matching the layout of the user's uploaded mockups
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .padding(bottom = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = title,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Page Indicators (minimalist dots/capsules exactly like the mock image)
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                for (i in 1..3) {
                                    val isActive = step == i
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(width = if (isActive) 18.dp else 8.dp, height = 8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isActive) RoyalBlue else Color(0xFFE2E8F0))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Small prominent title
                            Text(
                                text = title,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Compact description
                            Text(
                                text = description,
                                fontSize = 15.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Bottom CTA action button (Full-width, tall height, highly polished)
                val buttonText = when (currentStep) {
                    1 -> "Continue"
                    2 -> if (hasNotificationPermission) "Next" else "Allow Notifications"
                    else -> if (hasOverlayPermission) "Get Started" else "Allow Overlay Screen"
                }

                val isButtonEnabled = when (currentStep) {
                    1 -> true
                    2 -> true // Clickable to request if false, clickable to next if true
                    else -> true // Clickable to request/open settings if false, clickable to navigate if true
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> currentStep = 2
                            2 -> {
                                if (hasNotificationPermission) {
                                    currentStep = 3
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        hasNotificationPermission = true
                                        currentStep = 3
                                    }
                                }
                            }
                            3 -> {
                                if (hasOverlayPermission) {
                                    // Complete splash onboarding and move to profile configuration onboarding screen
                                    navController.navigate("onboarding") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    // Open Draw Over Other Apps Settings screen
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        hasOverlayPermission = true
                                        navController.navigate("onboarding") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = Color.White,
                        disabledContainerColor = RoyalBlue.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buttonText.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun hasNotificationPermissionCheck(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun hasOverlayPermissionCheck(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}
