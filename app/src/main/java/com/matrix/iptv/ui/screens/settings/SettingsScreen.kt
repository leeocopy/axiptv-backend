package com.matrix.iptv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.matrix.iptv.ui.theme.matrixColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val mx = MaterialTheme.matrixColors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mx.bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Settings Screen",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Configuration and Profiles",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("Back")
            }
        }
    }
}
