package com.matrix.iptv.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ── Custom extended colors (non-Material3 tokens from skill_2.md) ────────────
data class MatrixColors(
    val bgPrimary:    Color = BgPrimary,
    val bgSurface:    Color = BgSurface,
    val bgElevated:   Color = BgElevated,
    val accentPink:   Color = AccentPink,
    val accentPurple: Color = AccentPurple,
    val accentOrange: Color = AccentOrange,
    val textPrimary:  Color = TextPrimary,
    val textSecondary:Color = TextSecondary,
    val textMuted:    Color = TextMuted,
    val focusGlow:    Color = FocusGlow,
    val success:      Color = ColorSuccess,
    val error:        Color = ColorError,
    val divider:      Color = ColorDivider,
)

// ── Material3 dark color scheme mapped to Matrix palette ─────────────────────
val matrixDarkColorScheme = darkColorScheme(
    background          = BgPrimary,
    surface             = BgSurface,
    surfaceVariant      = BgElevated,
    primary             = AccentPink,
    secondary           = AccentPurple,
    tertiary            = AccentOrange,
    onBackground        = TextPrimary,
    onSurface           = TextPrimary,
    onSurfaceVariant    = TextSecondary,
    onPrimary           = TextPrimary,
    onSecondary         = TextPrimary,
    error               = ColorError,
    onError             = TextPrimary,
    outline             = ColorDivider,
)
