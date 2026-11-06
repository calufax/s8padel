package com.calufax.s8padel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PadelEmerald = Color(0xFF0B6E4F)
val PadelEmeraldDark = Color(0xFF074A35)
val PadelEmeraldLight = Color(0xFF4AD295)
val PadelBallLime = Color(0xFFD4F238)

val Court1AccentLight = Color(0xFF0D5C9E)
val Court1ContainerLight = Color(0xFFE3F2FD)
val Court2AccentLight = Color(0xFF9C4221)
val Court2ContainerLight = Color(0xFFFFF0E6)

val Court1AccentDark = Color(0xFF82B1FF)
val Court1ContainerDark = Color(0xFF102A43)
val Court2AccentDark = Color(0xFFFFB088)
val Court2ContainerDark = Color(0xFF3B1D11)

private val LightColors = lightColorScheme(
    primary = PadelEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2F4E6),
    onPrimaryContainer = Color(0xFF043826),
    secondary = Color(0xFF1E6091),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9ECFA),
    onSecondaryContainer = Color(0xFF092C47),
    tertiary = Color(0xFF7A5900),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF99),
    onTertiaryContainer = Color(0xFF261900),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF161D1A),
    surface = Color.White,
    onSurface = Color(0xFF161D1A),
    surfaceVariant = Color(0xFFE7EFEA),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707974)
)

private val DarkColors = darkColorScheme(
    primary = PadelEmeraldLight,
    onPrimary = Color(0xFF003825),
    primaryContainer = PadelEmeraldDark,
    onPrimaryContainer = Color(0xFFC8F7E3),
    secondary = Color(0xFF8ECAE6),
    onSecondary = Color(0xFF00344C),
    secondaryContainer = Color(0xFF134B6B),
    onSecondaryContainer = Color(0xFFCFE9F8),
    tertiary = Color(0xFFF4D35E),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = Color(0xFFFFDF99),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFE0E6E2),
    surface = Color(0xFF171F1B),
    onSurface = Color(0xFFE0E6E2),
    surfaceVariant = Color(0xFF25302B),
    onSurfaceVariant = Color(0xFFBFC9C3),
    outline = Color(0xFF89938E)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)

@Composable
fun S8PadelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
