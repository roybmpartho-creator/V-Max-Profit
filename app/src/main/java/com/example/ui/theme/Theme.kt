package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF003646),
    onPrimaryContainer = Color(0xFFB8EFFF),
    secondary = GoldAccent,
    onSecondary = Color(0xFF261900),
    secondaryContainer = Color(0xFF422C00),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun NFTTradeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
