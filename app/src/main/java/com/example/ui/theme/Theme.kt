package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FuturisticColorScheme = darkColorScheme(
    primary = CyberPurple,
    secondary = CyberCyan,
    tertiary = CyberPink,
    background = DeepSpaceBg,
    surface = CardCyber,
    onPrimary = DeepSpaceBg,
    onSecondary = DeepSpaceBg,
    onTertiary = Color.White,
    onBackground = Color(0xFFF0E8FF),
    onSurface = Color(0xFFF5F0FF),
    outline = BorderCyber
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FuturisticColorScheme,
        typography = Typography,
        content = content
    )
}
