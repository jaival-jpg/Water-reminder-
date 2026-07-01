package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanAccent,
    secondary = AquaBlue,
    tertiary = RoyalBlue,
    background = DarkBlueBackground,
    surface = DeepOceanBlue,
    onPrimary = DarkBlueBackground,
    onSecondary = DarkBlueBackground,
    onTertiary = SoftWhite,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalBlue,
    secondary = CyanAccent,
    tertiary = DeepOceanBlue,
    background = SoftWhite,
    surface = Color.White,
    onPrimary = SoftWhite,
    onSecondary = DarkBlueBackground,
    onTertiary = SoftWhite,
    onBackground = DarkBlueBackground,
    onSurface = DarkBlueBackground,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force our custom theme by ignoring dynamic color
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
