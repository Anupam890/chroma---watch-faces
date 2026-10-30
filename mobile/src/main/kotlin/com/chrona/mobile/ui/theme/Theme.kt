package com.chrona.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Brand palette — "Midnight Atelier": deep ink, violet light, coral accent, warm cream text.
object ChronaColors {
    val Ink = Color(0xFF0B0B12)
    val InkRaised = Color(0xFF15152A)
    val InkCard = Color(0xFF1C1C33)
    val Violet = Color(0xFF7C5CFF)
    val VioletSoft = Color(0xFF9D86FF)
    val Coral = Color(0xFFFF5E5B)
    val Mint = Color(0xFF5BE7C4)
    val Amber = Color(0xFFFFC857)
    val Sky = Color(0xFF4CC9F0)
    val Cream = Color(0xFFF7F3EA)
    val CreamMuted = Color(0xFFB8B4AE)
    val Paper = Color(0xFFFCFBF8)
    val PaperRaised = Color(0xFFF1EEE7)
}

private val DarkScheme = darkColorScheme(
    primary = ChronaColors.Violet,
    onPrimary = Color.White,
    primaryContainer = ChronaColors.InkCard,
    onPrimaryContainer = ChronaColors.Cream,
    secondary = ChronaColors.Coral,
    onSecondary = Color.White,
    tertiary = ChronaColors.Mint,
    background = ChronaColors.Ink,
    onBackground = ChronaColors.Cream,
    surface = ChronaColors.Ink,
    onSurface = ChronaColors.Cream,
    surfaceVariant = ChronaColors.InkRaised,
    onSurfaceVariant = ChronaColors.CreamMuted,
    surfaceContainer = ChronaColors.InkRaised,
    surfaceContainerHigh = ChronaColors.InkCard,
    outline = Color(0xFF2E2E4A),
    error = ChronaColors.Coral
)

private val LightScheme = lightColorScheme(
    primary = ChronaColors.Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9E3FF),
    onPrimaryContainer = Color(0xFF1C0F4D),
    secondary = ChronaColors.Coral,
    tertiary = Color(0xFF0F8C6E),
    background = ChronaColors.Paper,
    onBackground = Color(0xFF14141C),
    surface = ChronaColors.Paper,
    onSurface = Color(0xFF14141C),
    surfaceVariant = ChronaColors.PaperRaised,
    onSurfaceVariant = Color(0xFF5B5A66),
    surfaceContainer = ChronaColors.PaperRaised,
    surfaceContainerHigh = Color(0xFFE8E4DC),
    outline = Color(0xFFD5D0C6),
    error = Color(0xFFC62828)
)

// Editorial type scale: tight display headings, relaxed body.
val ChronaTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.2.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.8.sp)
)

@Composable
fun ChronaTheme(themeMode: String = "system", content: @Composable () -> Unit) {
    val dark = when (themeMode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = ChronaTypography,
        content = content
    )
}
