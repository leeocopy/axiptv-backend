package com.matrix.iptv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matrix.iptv.ui.theme.FocusGlow
import com.matrix.iptv.ui.theme.matrixColors

/**
 * MatrixTileCard — skill_2.md §6.4 Refined
 * Premium poster-style card for Android TV.
 */
@Composable
fun MatrixTileCard(
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(200),
        label = "tile_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) FocusGlow else Color.Transparent,
        animationSpec = tween(200),
        label = "border_color"
    )

    val fontSize = if (isSmall) 14.sp else 18.sp
    val iconSize = if (isSmall) 32.dp else 40.dp
    val padding = if (isSmall) 12.dp else 16.dp
    val spacing = if (isSmall) 8.dp else 12.dp

    Box(
        modifier = modifier
            .padding(8.dp)
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 20.dp else 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = if (isFocused) FocusGlow else Color.Black,
                spotColor = if (isFocused) FocusGlow else Color.Black
            )
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(28.dp)
            )
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .focusable()
    ) {
        // 1. Background (Premium Abstract Gradient as fallback for missing assets)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush = Brush.linearGradient(
                colors = gradientColors,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height)
            )
            drawRect(brush = brush)
            
            // Subtle "texture" or patterns
            if (!isFocused) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.minDimension / 2,
                    center = androidx.compose.ui.geometry.Offset(size.width, 0f)
                )
            }
        }

        // 2. Dark Overlay / Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // 3. Content (Bottom Left)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // White Circular Icon Badge
            Surface(
                modifier = Modifier.size(iconSize),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = gradientColors.firstOrNull() ?: Color.Gray,
                        modifier = Modifier.size(iconSize * 0.55f)
                    )
                }
            }
            
            Spacer(Modifier.width(spacing))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    letterSpacing = if (isSmall) (-0.5).sp else 0.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                softWrap = false
            )
        }
        
        // 4. Focus Glow Overlay (Subtle inner highlight)
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(28.dp)
                    )
            )
        }
    }
}
