package com.k3h.yoke.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Light palette: white background, black text, blues for everything else ----
val YokeWhite = Color(0xFFFFFFFF)
val YokeInk = Color(0xFF000000)
val YokeBlue = Color(0xFF1A5FCC)
val YokeBlueDeep = Color(0xFF0B2F6B)
val YokeBlueBright = Color(0xFF0277BD)
val YokeBlueTint = Color(0xFFDCEBFB)
val YokeBlueMist = Color(0xFFF1F6FD)
val YokeBlueLine = Color(0xFFB3CCEE)

private val YokeLightColorScheme = lightColorScheme(
    primary = YokeBlue,
    onPrimary = YokeWhite,
    primaryContainer = YokeBlueTint,
    onPrimaryContainer = YokeInk,
    secondary = YokeBlueDeep,
    onSecondary = YokeWhite,
    secondaryContainer = YokeBlue,        // selected chips and the selected navbar item
    onSecondaryContainer = YokeWhite,
    tertiary = YokeBlueBright,
    onTertiary = YokeWhite,
    background = YokeWhite,
    onBackground = YokeInk,
    surface = YokeWhite,
    onSurface = YokeInk,
    surfaceVariant = YokeBlueMist,
    onSurfaceVariant = YokeInk,
    outline = YokeBlueDeep,
    outlineVariant = YokeBlueLine
)

// ---- Dark palette: near-black background, white text, lighter blues for contrast ----
private val YokeDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6EA8FF),
    onPrimary = Color(0xFF001A3D),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF2F6FDB),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2F6FDB),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF0B6BAE),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF151B23),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1B2430),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFF7FAAE8),
    outlineVariant = Color(0xFF2C3A4D)
)

// ---- Typography (the logo takes the current main colour) ----
private fun yokeTypography(logoColor: Color) = Typography(
    displayLarge = TextStyle(            // "YOKE" logo
        fontFamily = FontFamily.Default,
        fontSize = 100.sp,
        color = logoColor
    ),
    headlineLarge = TextStyle(           // page titles
        fontFamily = FontFamily.Default,
        fontSize = 40.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
)

// ---- Shapes ----
private val YokeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

object YokeStyle {
    val borderWidth = 2.dp
    val cardShape = RoundedCornerShape(8.dp)
    val buttonShape = RoundedCornerShape(6.dp)
    val smallShape = RoundedCornerShape(4.dp)
}

@Composable
fun YokeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) YokeDarkColorScheme else YokeLightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = yokeTypography(colors.primary),
        shapes = YokeShapes,
        content = content
    )
}