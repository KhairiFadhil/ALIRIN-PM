package com.example.alirinmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AlirinColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = PrimaryInk,
    secondary = Sky,
    onSecondary = SkyInk,
    secondaryContainer = SkySoft,
    onSecondaryContainer = SkyInk,
    tertiary = Amber,
    onTertiary = Surface,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = AmberInk,
    background = Bg,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Surface2,
    onSurfaceVariant = Ink2,
    outline = Hairline2,
    outlineVariant = Hairline,
    error = RiskKritisDot,
    onError = Surface,
)

@Composable
fun ALIRINMOBILETheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlirinColorScheme,
        typography = AlirinTypography,
        shapes = AlirinShapes,
        content = content,
    )
}
