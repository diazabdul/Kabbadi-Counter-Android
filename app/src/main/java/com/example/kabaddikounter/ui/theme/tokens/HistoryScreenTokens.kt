package com.example.kabaddikounter.ui.theme.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.kabaddikounter.ui.theme.Neg
import com.example.kabaddikounter.ui.theme.Paper
import com.example.kabaddikounter.ui.theme.Raid

@Immutable
data class HistoryScreenTokens(
    val emptyStateText: Color,
    val deleteRevealContainer: Color,
    val deleteRevealContent: Color,
    val cardContainer: Color,
    val cardMetaText: Color,
    val cardTitleText: Color,
    val cardMutedText: Color,
    val winnerScore: Color,
    val loserScore: Color,
    val statusBadgeBorder: Color,
    val statusBadgeText: Color,
)

@Composable
fun historyScreenTokens(
    colorScheme: ColorScheme,
): HistoryScreenTokens {
    val app = appUiTokens(colorScheme)
    val mode = colorScheme.uiColorMode()

    return HistoryScreenTokens(
        emptyStateText = app.mutedText,
        deleteRevealContainer = if (mode == UiColorMode.Dark) colorScheme.errorContainer else app.dangerActionContainer,
        deleteRevealContent = if (mode == UiColorMode.Dark) colorScheme.onErrorContainer else app.dangerActionContent,
        cardContainer = if (mode == UiColorMode.Dark) app.cardSecondaryContainer else Paper,
        cardMetaText = app.mutedText,
        cardTitleText = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
        cardMutedText = app.mutedText,
        winnerScore = if (mode == UiColorMode.Dark) Raid else Neg,
        loserScore = app.mutedText,
        statusBadgeBorder = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
        statusBadgeText = if (mode == UiColorMode.Dark) app.cardSecondaryContent else colorScheme.onBackground,
    )
}
