package com.example.kabaddikounter.ui.theme.tokens

import androidx.compose.material3.ColorScheme
import com.example.kabaddikounter.ui.theme.Jet

enum class UiColorMode {
    Light,
    Dark,
}

internal fun ColorScheme.uiColorMode(): UiColorMode {
    return if (background == Jet) UiColorMode.Dark else UiColorMode.Light
}
