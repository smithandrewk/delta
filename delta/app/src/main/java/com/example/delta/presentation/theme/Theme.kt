package com.example.delta.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

import androidx.wear.compose.material.Colors

/**
 * Custom Theme for Wear App. Read comments below for why shape isn't included.
 */
@Composable
fun WearAppTheme(
    colors: Colors = initialThemeValues.colors,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = colors,
        typography = WearTypography,
        // For shapes, we generally recommend using the default Material Wear shapes which are
        // optimized for round and non-round devices.
        content = content
    )
}
