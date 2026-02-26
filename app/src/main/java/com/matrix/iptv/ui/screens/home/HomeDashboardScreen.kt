package com.matrix.iptv.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matrix.iptv.ui.components.BrandLogo
import com.matrix.iptv.ui.components.MatrixTileCard
import com.matrix.iptv.ui.navigation.Screen
import com.matrix.iptv.ui.theme.AccentOrange
import com.matrix.iptv.ui.theme.AccentPink
import com.matrix.iptv.ui.theme.matrixColors
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HomeDashboardScreen(
    onNavigate: (String) -> Unit
) {
    val mx = MaterialTheme.matrixColors

    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {
        DottedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 20.dp)
        ) {
            DashboardHeader()
            
            Spacer(Modifier.height(32.dp))

            DashboardGrid(onNavigate)

            Spacer(Modifier.weight(1f))

            RemoteHintRow()
        }
    }
}

@Composable
fun DottedBackground() {
    val dotColor = Color.White.copy(alpha = 0.03f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dotRadius = 0.8.dp.toPx()
        val spacing = 32.dp.toPx()
        
        for (x in 0..(size.width / spacing).toInt()) {
            for (y in 0..(size.height / spacing).toInt()) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x * spacing, y * spacing)
                )
            }
        }
    }
}

@Composable
fun DashboardHeader() {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy | EEEE", Locale.ENGLISH)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Time
        Text(
            text = currentTime.format(timeFormatter).uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = Color.White.copy(alpha = 0.7f)
        )

        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(size = 32.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "AXIPTV",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                ),
                color = Color.White
            )
        }

        // Date
        Text(
            text = currentTime.format(dateFormatter),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DashboardGrid(onNavigate: (String) -> Unit) {
    val cardHeight = 160.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(12),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // --- TOP ROW ---
        item(span = { GridItemSpan(4) }) {
            MatrixTileCard(
                title = "Movies",
                icon = Icons.Default.Movie,
                gradientColors = listOf(Color(0xFFFF8C42), Color(0xFFFF3C5F)),
                onClick = { onNavigate(Screen.VodCategories.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(4) }) {
            MatrixTileCard(
                title = "Live",
                icon = Icons.Default.Public,
                gradientColors = listOf(Color(0xFF4A90E2), Color(0xFF9013FE)),
                onClick = { onNavigate(Screen.LiveCategories.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(2) }) {
            MatrixTileCard(
                title = "Favourite",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
                onClick = { onNavigate(Screen.Favorites.route) },
                modifier = Modifier.height(cardHeight),
                isSmall = true
            )
        }
        item(span = { GridItemSpan(2) }) {
            MatrixTileCard(
                title = "Series",
                icon = Icons.Default.Tv,
                gradientColors = listOf(Color(0xFFF8D800), Color(0xFFF57F17)),
                onClick = { onNavigate(Screen.SeriesCategories.route) },
                modifier = Modifier.height(cardHeight),
                isSmall = true
            )
        }

        // --- SECOND ROW ---
        item(span = { GridItemSpan(2) }) {
            MatrixTileCard(
                title = "Music",
                icon = Icons.Default.MusicNote,
                gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                onClick = { onNavigate(Screen.Music.route) },
                modifier = Modifier.height(cardHeight),
                isSmall = true
            )
        }
        item(span = { GridItemSpan(2) }) {
            MatrixTileCard(
                title = "Entertainment",
                icon = Icons.Default.Extension,
                gradientColors = listOf(Color(0xFFF02FC2), Color(0xFF6094ea)),
                onClick = { onNavigate(Screen.Entertainment.route) },
                modifier = Modifier.height(cardHeight),
                isSmall = true
            )
        }
        item(span = { GridItemSpan(2) }) {
            MatrixTileCard(
                title = "Radio",
                icon = Icons.Default.Radio,
                gradientColors = listOf(Color(0xFFf6d365), Color(0xFFfda085)),
                onClick = { onNavigate(Screen.Radio.route) },
                modifier = Modifier.height(cardHeight),
                isSmall = true
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Devotional",
                icon = Icons.Default.AutoStories,
                gradientColors = listOf(Color(0xFF30cfd0), Color(0xFF330867)),
                onClick = { onNavigate(Screen.Devotional.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Settings",
                icon = Icons.Default.Settings,
                gradientColors = listOf(Color(0xFFee9ca7), Color(0xFFffafbd)),
                onClick = { onNavigate(Screen.Settings.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
    }
}

@Composable
fun RemoteHintRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HintItem(Icons.Default.ArrowDropUp, "Up")
        HintItem(Icons.Default.ArrowDropDown, "Down")
        HintItem(Icons.Default.ArrowLeft, "Left")
        HintItem(Icons.Default.ArrowRight, "Right")
        Spacer(Modifier.width(24.dp))
        HintItemText("OK", "Select")
        Spacer(Modifier.width(16.dp))
        HintItem(Icons.Default.Undo, "Exit")
    }
}

@Composable
fun HintItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun HintItemText(key: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(24.dp).padding(horizontal = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
