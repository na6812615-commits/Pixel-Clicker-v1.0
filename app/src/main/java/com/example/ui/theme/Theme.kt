package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val PixieColorScheme = darkColorScheme(
    primary = PixieMint,
    onPrimary = PixieBgDeep,
    primaryContainer = PixieMintDark,
    onPrimaryContainer = PixieMint,
    secondary = PixieRose,
    onSecondary = PixieBgDeep,
    secondaryContainer = PixieRoseDark,
    onSecondaryContainer = PixieRose,
    tertiary = PixieGold,
    onTertiary = PixieBgDeep,
    tertiaryContainer = PixieGoldDark,
    onTertiaryContainer = PixieGold,
    background = PixieBgDeep,
    onBackground = PixieTextPrimary,
    surface = PixieBgSurface,
    onSurface = PixieTextPrimary,
    surfaceVariant = PixieBgCard,
    onSurfaceVariant = PixieTextSecondary,
    outline = PixieBorder,
    outlineVariant = PixieBorderBright,
    error = PixieDanger,
    onError = PixieBgDeep,
    errorContainer = PixieDangerDark,
    onErrorContainer = PixieDanger
)

val PixieShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun PixieClickerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PixieColorScheme,
        typography = Typography,
        shapes = PixieShapes,
        content = content
    )
}
