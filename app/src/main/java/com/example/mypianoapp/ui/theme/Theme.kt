package com.example.mypianoapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PianoColorScheme = darkColorScheme(
    primary          = KeysViolet,
    onPrimary        = TextPrimary,
    primaryContainer = KeysVioletGlow,
    onPrimaryContainer = KeysVioletLight,

    secondary        = IvoryGold,
    onSecondary      = EbonyDeep,
    secondaryContainer = IvoryGoldGlow,
    onSecondaryContainer = IvoryGoldLight,

    tertiary         = NotesTeal,
    onTertiary       = EbonyDeep,
    tertiaryContainer = NotesTealGlow,
    onTertiaryContainer = NotesTeal,

    background       = EbonyDeep,
    onBackground     = TextPrimary,

    surface          = EbonyCard,
    onSurface        = TextPrimary,
    surfaceVariant   = EbonySurface,
    onSurfaceVariant = TextSecondary,

    outline          = EbonyBorder,
    outlineVariant   = EbonyBorder,

    error            = DissonanceRed,
    onError          = TextPrimary,
)

@Composable
fun MyPianoAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PianoColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
