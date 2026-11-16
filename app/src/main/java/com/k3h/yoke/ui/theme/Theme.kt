package com.k3h.yoke.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Palette ----
val YokeBlack = Color(0xFF000000)
val YokeWhite = Color(0xFFFFFFFF)
val YokeGrey = Color(0xFFF2F2F2)
val YokeMidGrey = Color(0xFFBDBDBD)

private val YokeColorScheme = lightColorScheme(
    primary = YokeBlack,
    onPrimary = YokeWhite,
    secondary = YokeBlack,
    onSecondary = YokeWhite,
    // used for selected chips and the selected navbar item
    secondaryContainer = YokeBlack,
    onSecondaryContainer = YokeWhite,
    background = YokeWhite,
    onBackground = YokeBlack,
    surface = YokeWhite,
    onSurface = YokeBlack,
    surfaceVariant = YokeGrey,
    onSurfaceVariant = YokeBlack,
    outline = YokeBlack,
    outlineVariant = YokeMidGrey
)

// ---- Typography ----
// Swap FontFamily.Default for your own font later (res/font) and every screen follows.
private val YokeTypography = Typography(
    displayLarge = TextStyle(            // "YOKE" logo
        fontFamily = FontFamily.Default,
        fontSize = 100.sp
    ),
    headlineLarge = TextStyle(           // page titles
        fontFamily = FontFamily.Default,
        fontSize = 40.sp
    ),
    headlineSmall = TextStyle(           // section headings
        fontFamily = FontFamily.Default,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(              // airports, route names
        fontFamily = FontFamily.Default,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(             // setting labels
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(               // dates, durations, captions
        fontFamily = FontFamily.Default,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(              // input labels, buttons
        fontFamily = FontFamily.Default,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
)

// ---- Shapes (affects text fields, chips, menus, etc.) ----
private val YokeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

// ---- Reusable style values for things Material doesn't theme for us ----
object YokeStyle {
    val borderWidth = 2.dp
    val cardShape = RoundedCornerShape(8.dp)
    val buttonShape = RoundedCornerShape(6.dp)
    val smallShape = RoundedCornerShape(4.dp)
}

@Composable
fun YokeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YokeColorScheme,
        typography = YokeTypography,
        shapes = YokeShapes,
        content = content
    )
}