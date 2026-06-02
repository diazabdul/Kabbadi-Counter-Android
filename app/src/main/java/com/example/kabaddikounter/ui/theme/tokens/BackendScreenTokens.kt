package com.example.kabaddikounter.ui.theme.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.kabaddikounter.ui.theme.Neg
import com.example.kabaddikounter.ui.theme.Paper
import com.example.kabaddikounter.ui.theme.Raid

@Immutable
data class BackendScreenTokens(
    val emptyStateText: Color,
    val errorStateText: Color,
    val cardContainer: Color,
    val cardMetaText: Color,
    val cardTitleText: Color,
    val cardMutedText: Color,
    val winnerScore: Color,
    val loserScore: Color,
    val divider: Color,
    val statusBadgeLiveBorder: Color,
    val statusBadgeLiveText: Color,
    val statusBadgeDefaultBorder: Color,
    val statusBadgeDefaultText: Color,
)

@Composable
fun backendScreenTokens(
    colorScheme: ColorScheme,
): BackendScreenTokens {
    val app = appUiTokens(colorScheme)
    val mode = colorScheme.uiColorMode()

    return BackendScreenTokens(
        emptyStateText = app.mutedText,
        errorStateText = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
        cardContainer = if (mode == UiColorMode.Dark) app.cardSecondaryContainer else Paper,
        cardMetaText = app.mutedText,
        cardTitleText = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
        cardMutedText = app.mutedText,
        winnerScore = if (mode == UiColorMode.Dark) Raid else Neg,
        loserScore = app.mutedText,
        divider = colorScheme.outlineVariant,
        statusBadgeLiveBorder = colorScheme.error,
        statusBadgeLiveText = colorScheme.error,
        statusBadgeDefaultBorder = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
        statusBadgeDefaultText = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
    )
}
