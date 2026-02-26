package com.matrix.iptv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matrix.iptv.ui.theme.AccentOrange
import com.matrix.iptv.ui.theme.AccentPink
import com.matrix.iptv.ui.theme.matrixColors

@Composable
fun BrandLogo(
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val mx = MaterialTheme.matrixColors
    val gradient = Brush.linearGradient(listOf(AccentPink, AccentOrange))

    Box(
        modifier = modifier
            .size(size)
            .background(brush = gradient, shape = RoundedCornerShape(size * 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AXIPTV",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = (size.value * 0.22f).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (size.value * 0.02f).sp
                ),
                color = Color.White
            )
        }
    }
}
