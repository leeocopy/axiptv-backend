package com.matrix.iptv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.matrix.iptv.ui.theme.matrixColors

/**
 * TV-aware focusable card — skill_2.md §2.1
 * Focus state: scale 1.08 + pink glow border, 150ms ease-out
 */
@Composable
fun FocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(150),
        label = "card_scale"
    )
    val mx = MaterialTheme.matrixColors

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(),
        colors = CardDefaults.cardColors(containerColor = mx.bgSurface),
        border = if (isFocused) BorderStroke(2.5.dp, mx.focusGlow) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            focusedElevation = 12.dp
        )
    ) {
        Box(content = content)
    }
}
