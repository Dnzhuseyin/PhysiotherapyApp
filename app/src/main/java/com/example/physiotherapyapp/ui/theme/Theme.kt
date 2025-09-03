package com.example.physiotherapyapp.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = HealthyBlue80,
    secondary = MedicalGreen80,
    tertiary = WarmAccent80,
    background = DarkGray,
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onPrimary = Color.White,
    onSecondary = DarkGray,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = HealthyBlue40,
    secondary = MedicalGreen40,
    tertiary = WarmAccent40,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariant,
    primaryContainer = Color(0xFFE3F2FD),
    secondaryContainer = Color(0xFFE8F5E8),
    tertiaryContainer = Color(0xFFFFF3E0),
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = DarkGray,
    onSurface = TextGray,
    onSurfaceVariant = Color(0xFF64748B),
    onPrimaryContainer = HealthyBlue40,
    onSecondaryContainer = MedicalGreen40,
    onTertiaryContainer = WarmAccent40,
    outline = MediumGray,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun PhysiotherapyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}