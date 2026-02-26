package com.matrix.iptv.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── skill_2.md §1.3 Typography ──────────────────────────────────────────────
val MatrixTypography = Typography(
    displayLarge  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold,     color = TextPrimary),
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleLarge    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium,   color = TextPrimary),
    bodyLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal,   color = TextPrimary),
    bodyMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   color = TextSecondary),
    labelLarge    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    labelSmall    = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,   color = TextMuted),
)
