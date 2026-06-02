package com.example.kabaddikounter.ui.theme.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppUiTokens(
    val inputText: Color,
    val inputLabel: Color,
    val inputFocusedLabel: Color,
    val inputCursor: Color,
    val inputFocusedBorder: Color,
    val inputUnfocusedBorder: Color,
    val cardPrimaryContainer: Color,
    val cardPrimaryContent: Color,
    val cardSecondaryContainer: Color,
    val cardSecondaryContent: Color,
    val accentOutlineBorder: Color,
    val accentOutlineContent: Color,
    val accentFilledContainer: Color,
    val accentFilledContent: Color,
    val primaryActionContainer: Color,
    val primaryActionContent: Color,
    val dangerActionContainer: Color,
    val dangerActionContent: Color,
    val badgeLiveContainer: Color,
    val badgeLiveContent: Color,
    val badgeFinalContainer: Color,
    val badgeFinalContent: Color,
    val mutedText: Color,
)

@Composable
fun appUiTokens(
    colorScheme: ColorScheme,
): AppUiTokens {
    return when (colorScheme.uiColorMode()) {
        UiColorMode.Dark -> AppUiTokens(
            inputText = colorScheme.onSurface,
            inputLabel = colorScheme.onSurfaceVariant,
            inputFocusedLabel = colorScheme.onSurface,
            inputCursor = colorScheme.primary,
            inputFocusedBorder = colorScheme.primary,
            inputUnfocusedBorder = colorScheme.outline,
            cardPrimaryContainer = colorScheme.primary,
            cardPrimaryContent = colorScheme.onPrimary,
            cardSecondaryContainer = colorScheme.surface,
            cardSecondaryContent = colorScheme.onSurface,
            accentOutlineBorder = colorScheme.onPrimary,
            accentOutlineContent = colorScheme.onPrimary,
            accentFilledContainer = colorScheme.onPrimary,
            accentFilledContent = colorScheme.primaryContainer,
            primaryActionContainer = colorScheme.primary,
            primaryActionContent = colorScheme.onPrimary,
            dangerActionContainer = colorScheme.error,
            dangerActionContent = colorScheme.onError,
            badgeLiveContainer = colorScheme.error,
            badgeLiveContent = colorScheme.onError,
            badgeFinalContainer = colorScheme.surfaceVariant,
            badgeFinalContent = colorScheme.onSurfaceVariant,
            mutedText = colorScheme.onSurfaceVariant,
        )

        UiColorMode.Light -> AppUiTokens(
            inputText = colorScheme.onBackground,
            inputLabel = colorScheme.onSurfaceVariant,
            inputFocusedLabel = colorScheme.onBackground,
            inputCursor = colorScheme.onBackground,
            inputFocusedBorder = colorScheme.onBackground,
            inputUnfocusedBorder = colorScheme.outline,
            cardPrimaryContainer = colorScheme.primaryContainer,
            cardPrimaryContent = colorScheme.onPrimaryContainer,
            cardSecondaryContainer = colorScheme.surface,
            cardSecondaryContent = colorScheme.onSurface,
            accentOutlineBorder = colorScheme.outline,
            accentOutlineContent = colorScheme.onSurface,
            accentFilledContainer = colorScheme.primary,
            accentFilledContent = colorScheme.onPrimary,
            primaryActionContainer = colorScheme.primary,
            primaryActionContent = colorScheme.onPrimary,
            dangerActionContainer = colorScheme.error,
            dangerActionContent = colorScheme.onError,
            badgeLiveContainer = colorScheme.error,
            badgeLiveContent = colorScheme.onError,
            badgeFinalContainer = colorScheme.surfaceVariant,
            badgeFinalContent = colorScheme.onSurfaceVariant,
            mutedText = colorScheme.onSurfaceVariant,
        )
    }
}
