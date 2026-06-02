package com.example.kabaddikounter.ui.theme.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.kabaddikounter.ui.theme.Paper

@Immutable
data class SettingsScreenTokens(
    val sectionLabel: Color,
    val cardContainer: Color,
    val divider: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val destructiveText: Color,
    val footerText: Color,
    val segmentedTrack: Color,
    val segmentedSelectedContainer: Color,
    val segmentedSelectedContent: Color,
    val segmentedUnselectedContent: Color,
    val trailingIcon: Color,
)

@Composable
fun settingsScreenTokens(
    colorScheme: ColorScheme,
): SettingsScreenTokens {
    val app = appUiTokens(colorScheme)
    val mode = colorScheme.uiColorMode()

    return if (mode == UiColorMode.Dark) {
        SettingsScreenTokens(
            sectionLabel = colorScheme.primary,
            cardContainer = colorScheme.surfaceContainer,
            divider = colorScheme.outlineVariant,
            primaryText = colorScheme.onSurface,
            secondaryText = app.mutedText,
            destructiveText = app.dangerActionContainer,
            footerText = app.mutedText,
            segmentedTrack = colorScheme.surfaceContainer,
            segmentedSelectedContainer = colorScheme.primary,
            segmentedSelectedContent = colorScheme.onPrimary,
            segmentedUnselectedContent = app.mutedText,
            trailingIcon = app.mutedText,
        )
    } else {
        SettingsScreenTokens(
            sectionLabel = colorScheme.onBackground,
            cardContainer = Paper,
            divider = colorScheme.outlineVariant,
            primaryText = colorScheme.onBackground,
            secondaryText = app.mutedText,
            destructiveText = app.dangerActionContainer,
            footerText = app.mutedText,
            segmentedTrack = Paper,
            segmentedSelectedContainer = colorScheme.primaryContainer,
            segmentedSelectedContent = colorScheme.onPrimaryContainer,
            segmentedUnselectedContent = colorScheme.onBackground,
            trailingIcon = app.mutedText,
        )
    }
}
