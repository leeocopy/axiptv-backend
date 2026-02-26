package com.matrix.iptv.ui.theme

import androidx.compose.ui.graphics.Color

// ── skill_2.md §1.1 Color Palette ──────────────────────────────────────────
val BgPrimary      = Color(0xFF1A1A2E)
val BgSurface      = Color(0xFF22223A)
val BgElevated     = Color(0xFF2C2C48)
val AccentPink     = Color(0xFFE91E8C)
val AccentPurple   = Color(0xFF9B27AF)
val AccentOrange   = Color(0xFFFF6B35)
val TextPrimary    = Color(0xFFFFFFFF)
val TextSecondary  = Color(0xFFB0B0C8)
val TextMuted      = Color(0xFF606080)
val FocusGlow      = Color(0xFFE91E8C)
val ColorSuccess   = Color(0xFF4CAF50)
val ColorError     = Color(0xFFF44336)
val ColorDivider   = Color(0xFF333355)

// ── Gradient helpers (use with Brush.linearGradient) ────────────────────────
// GradientPrimary   = [AccentPink → AccentOrange]  angle 0°
// GradientCategory  = [AccentPurple → AccentPink]  angle 0°
// GradientCardScrim = [Transparent → #000000CC]   angle 270°
