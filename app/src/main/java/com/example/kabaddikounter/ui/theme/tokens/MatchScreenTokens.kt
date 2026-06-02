package com.example.kabaddikounter.ui.theme.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class MatchScreenTokens(
    val inputText: Color,
    val inputLabel: Color,
    val inputFocusedLabel: Color,
    val inputCursor: Color,
    val inputFocusedBorder: Color,
    val inputUnfocusedBorder: Color,
    val primaryCardContainer: Color,
    val primaryCardTitle: Color,
    val primaryCardScore: Color,
    val primaryCardOutlineButtonBorder: Color,
    val primaryCardOutlineButtonContent: Color,
    val primaryCardFilledButtonContainer: Color,
    val primaryCardFilledButtonContent: Color,
    val secondaryCardContainer: Color,
    val secondaryCardTitle: Color,
    val secondaryCardScore: Color,
    val secondaryCardOutlineButtonBorder: Color,
    val secondaryCardOutlineButtonContent: Color,
    val secondaryCardFilledButtonContainer: Color,
    val secondaryCardFilledButtonContent: Color,
    val liveBadgeContainer: Color,
    val liveBadgeContent: Color,
    val finalBadgeContainer: Color,
    val finalBadgeContent: Color,
)

@Composable
fun matchScreenTokens(
    colorScheme: ColorScheme,
): MatchScreenTokens {
    val app = appUiTokens(colorScheme)
    val mode = colorScheme.uiColorMode()
    return MatchScreenTokens(
        inputText = app.inputText,
        inputLabel = app.inputLabel,
        inputFocusedLabel = app.inputFocusedLabel,
        inputCursor = app.inputCursor,
        inputFocusedBorder = app.inputFocusedBorder,
        inputUnfocusedBorder = app.inputUnfocusedBorder,
        primaryCardContainer = app.cardPrimaryContainer,
        primaryCardTitle = app.cardPrimaryContent,
        primaryCardScore = if (mode == UiColorMode.Dark) colorScheme.surface else app.cardPrimaryContent,
        primaryCardOutlineButtonBorder = if (mode == UiColorMode.Dark) app.cardPrimaryContent else colorScheme.onPrimaryContainer,
        primaryCardOutlineButtonContent = if (mode == UiColorMode.Dark) app.cardPrimaryContent else colorScheme.onPrimaryContainer,
        primaryCardFilledButtonContainer = if (mode == UiColorMode.Dark) app.cardPrimaryContent else colorScheme.onPrimaryContainer,
        primaryCardFilledButtonContent = colorScheme.primaryContainer,
        secondaryCardContainer = app.cardSecondaryContainer,
        secondaryCardTitle = app.cardSecondaryContent,
        secondaryCardScore = app.cardSecondaryContent,
        secondaryCardOutlineButtonBorder = if (mode == UiColorMode.Dark) colorScheme.onSecondaryContainer else app.accentOutlineBorder,
        secondaryCardOutlineButtonContent = if (mode == UiColorMode.Dark) colorScheme.onSecondaryContainer else app.accentOutlineContent,
        secondaryCardFilledButtonContainer = if (mode == UiColorMode.Dark) colorScheme.onSecondaryContainer else app.accentFilledContainer,
        secondaryCardFilledButtonContent = if (mode == UiColorMode.Dark) colorScheme.onPrimary else app.accentFilledContent,
        liveBadgeContainer = app.badgeLiveContainer,
        liveBadgeContent = app.badgeLiveContent,
        finalBadgeContainer = app.badgeFinalContainer,
        finalBadgeContent = app.badgeFinalContent,
    )
}
