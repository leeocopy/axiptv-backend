package com.matrix.iptv.ui.screens.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matrix.iptv.ui.theme.FocusGlow
import com.matrix.iptv.ui.theme.matrixColors

@Composable
fun ContentHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
fun CategoryCard(
    name: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.1f else 1.0f)
    val mx = MaterialTheme.matrixColors

    Box(
        modifier = Modifier
            .padding(8.dp)
            .scale(scale)
            .aspectRatio(3f / 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) mx.accentPink else mx.bgSurface)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusGlow else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isFocused) Color.Black else Color.White
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun MediaCard(
    name: String,
    iconUrl: String?,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1.0f)
    val mx = MaterialTheme.matrixColors

    Column(
        modifier = Modifier
            .padding(8.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .focusable(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(mx.bgSurface)
                .border(
                    width = if (isFocused) 3.dp else 0.dp,
                    color = if (isFocused) FocusGlow else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for icon (AsyncImage would be better but keeping it simple)
            Text(
                text = name.take(1),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White.copy(alpha = 0.2f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                color = if (isFocused) FocusGlow else Color.White
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
