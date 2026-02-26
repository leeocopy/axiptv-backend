package com.matrix.iptv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.matrix.iptv.ui.theme.AccentOrange
import com.matrix.iptv.ui.theme.AccentPink
import com.matrix.iptv.ui.theme.matrixColors

private val PillShape = RoundedCornerShape(50)

/**
 * Primary CTA button — skill_2.md §1.8 PrimaryButton
 * GradientPrimary background, pill shape, optional TV chevron hints
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArrows: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(150),
        label = "btn_scale"
    )
    val gradient = Brush.horizontalGradient(listOf(AccentPink, AccentOrange))

    Box(
        modifier = modifier
            .scale(scale)
            .background(brush = gradient, shape = PillShape)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.matrixColors.textPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                if (showArrows) Icon(Icons.Default.ChevronLeft, null, Modifier.size(20.dp))
                Spacer(Modifier.width(if (showArrows) 8.dp else 0.dp))
                Text(text, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(if (showArrows) 8.dp else 0.dp))
                if (showArrows) Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Secondary button — skill_2.md §1.8 SecondaryButton
 * bgSurface + accentPink border
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(150),
        label = "sec_btn_scale"
    )
    val mx = MaterialTheme.matrixColors

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, mx.accentPink),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = mx.bgSurface,
            contentColor = mx.accentPink
        ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
        modifier = modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = mx.accentPink)
    }
}
