package com.matrix.iptv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── skill_2.md §1.5 Corner Radius ───────────────────────────────────────────
val RadiusSm   =  8.dp
val RadiusMd   = 12.dp
val RadiusLg   = 16.dp
val RadiusXl   = 24.dp
// radius-pill = 50% → use RoundedCornerShape(50%)

val MatrixShapes = Shapes(
    extraSmall = RoundedCornerShape(RadiusSm),
    small      = RoundedCornerShape(RadiusSm),
    medium     = RoundedCornerShape(RadiusMd),
    large      = RoundedCornerShape(RadiusLg),
    extraLarge = RoundedCornerShape(RadiusXl),
)
