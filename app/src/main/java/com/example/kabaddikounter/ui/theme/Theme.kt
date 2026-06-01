package com.example.kabaddikounter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

val KabaddiShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary                = Raid,
    onPrimary              = Jet,
    primaryContainer       = Raid2,
    onPrimaryContainer     = Jet,
    secondary              = Pos,
    onSecondary            = Jet,
    secondaryContainer     = Ink,
    onSecondaryContainer   = Cream,
    tertiary               = Bonus,
    onTertiary             = Jet,
    background             = Jet,
    onBackground           = Cream,
    surface                = Char,
    onSurface              = Cream,
    surfaceVariant         = Ink,
    onSurfaceVariant       = Mute,
    surfaceContainer       = Char,
    surfaceContainerHigh   = Ink,
    surfaceContainerHighest= Rule,
    outline                = Rule,
    outlineVariant         = Ink,
    error                  = Neg,
    onError                = Cream,
)

// Light mode: cream background, dark-card aesthetic (jet cards on cream)
private val LightColorScheme = lightColorScheme(
    primary                = Raid,
    onPrimary              = Jet,
    primaryContainer       = Raid2,
    onPrimaryContainer     = Jet,
    secondary              = Pos,
    onSecondary            = Jet,
    secondaryContainer     = Paper,
    onSecondaryContainer   = Jet,
    tertiary               = Bonus,
    onTertiary             = Jet,
    background             = Cream,
    onBackground           = Jet,
    surface                = Jet,
    onSurface              = Cream,
    surfaceVariant         = Char,
    onSurfaceVariant       = Mute,
    surfaceContainer       = Jet,
    surfaceContainerHigh   = Char,
    surfaceContainerHighest= Ink,
    outline                = Bone,
    outlineVariant         = Paper,
    error                  = Neg,
    onError                = Cream,
)

@Composable
fun KabaddiKounterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        shapes = KabaddiShapes,
        content = content
    )
}
