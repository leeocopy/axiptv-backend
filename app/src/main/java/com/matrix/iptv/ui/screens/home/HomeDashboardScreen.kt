package com.matrix.iptv.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.matrix.iptv.ui.components.BrandLogo
import com.matrix.iptv.ui.components.MatrixTileCard
import com.matrix.iptv.ui.navigation.Screen
import com.matrix.iptv.ui.theme.matrixColors
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HomeDashboardScreen(
    onNavigate: (String) -> Unit,
    onPlayContent: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onSeriesSelected: (Int) -> Unit = {},
    viewModel: HomeDashboardViewModel = hiltViewModel()
) {
    val mx = MaterialTheme.matrixColors

    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {
        DottedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 20.dp)
        ) {
            DashboardHeader(onNavigate)
            
            val state by viewModel.state.collectAsState()
            
            // New Update Dialog
            if (state.updateConfig != null) {
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                var showUpdate by remember { mutableStateOf(true) }
                if (showUpdate) {
                    AlertDialog(
                        onDismissRequest = { if (!state.updateConfig!!.isCritical) showUpdate = false },
                        title = { Text("Update Available", color = Color.White) },
                        text = { 
                            Column {
                                Text("Version ${state.updateConfig!!.latestVersion} is now available.", color = Color.White.copy(0.7f))
                                Spacer(Modifier.height(8.dp))
                                Text(state.updateConfig!!.updateMessage, color = Color.White.copy(0.9f))
                            }
                        },
                        containerColor = Color(0xFF1E1E1E),
                        confirmButton = {
                            Button(
                                onClick = { 
                                    uriHandler.openUri(state.updateConfig!!.updateUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = mx.accentHover)
                            ) {
                                Text("UPDATE NOW", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = if (!state.updateConfig!!.isCritical) {
                            {
                                TextButton(onClick = { showUpdate = false }) {
                                    Text("LATER", color = Color.White.copy(0.5f))
                                }
                            }
                        } else null
                    )
                }
            }

            if (state.watchHistory.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                ContinueWatchingRow(state.watchHistory, onPlayContent)
            }

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
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer() // allows Compose to cache this layer and skip redraw when nothing changes
    ) {
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
fun DashboardHeader(onNavigate: (String) -> Unit, viewModel: HomeDashboardViewModel = hiltViewModel()) {
    val currentTime = com.matrix.iptv.ui.components.LocalClock.current
    val profileName by viewModel.profileName.collectAsState()

    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val mx = MaterialTheme.matrixColors

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Logo (Bigger as requested)
        BrandLogo(size = 72.dp)
        
        Spacer(Modifier.width(24.dp))
        
        // 2. Profile Info / Switch Profile
        Surface(
            onClick = { onNavigate(Screen.ProfileFromHome.route) },
            shape = CircleShape,
            color = Color.White.copy(0.05f),
            modifier = Modifier.height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.AccountCircle, null, tint = mx.accentHover, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = profileName.ifBlank { "SWITCH PROFILE" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("ACTIVE ACCOUNT", color = Color.White.copy(0.4f), fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // 3. Search IconButton
        IconButton(
            onClick = { onNavigate(Screen.Search.route) },
            modifier = Modifier
                .padding(end = 16.dp)
                .background(Color.White.copy(0.05f), CircleShape)
        ) {
            Icon(Icons.Default.Search, "Search", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // 4. Time
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currentTime.format(timeFormatter),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = currentTime.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd", Locale.ENGLISH)).uppercase(),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ContinueWatchingRow(
    history: List<com.matrix.iptv.data.local.db.WatchHistoryEntity>,
    onPlayContent: (String, String, String, String, String) -> Unit
) {
    val mx = MaterialTheme.matrixColors
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayCircle, null, tint = mx.accentHover, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "CONTINUE WATCHING", 
                color = Color.White, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(history) { item ->
                ResumeCard(item) {
                    onPlayContent(item.contentId, item.type, item.title, item.extension ?: "mp4", item.categoryId ?: "0")
                }
            }
        }
    }
}

@Composable
fun ResumeCard(
    item: com.matrix.iptv.data.local.db.WatchHistoryEntity,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val mx = MaterialTheme.matrixColors
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E),
        modifier = Modifier
            .width(220.dp)
            .height(110.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) mx.accentHover else Color.White.copy(0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Content
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(60.dp, 86.dp),
                    color = Color.Black.copy(0.5f)
                ) {
                    AsyncImage(
                        model = item.icon ?: "",
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        item.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.type.uppercase(),
                        color = mx.accentHover,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Progress Bar at bottom
            val progress = if (item.durationMs > 0) item.positionMs.toFloat() / item.durationMs.toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(0.1f))
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(mx.accentHover)
                )
            }
        }
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
        // --- TOP ROW (12 columns total: 3 + 3 + 3 + 3) ---
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "All",
                icon = Icons.Default.Dashboard,
                gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                onClick = { onNavigate(Screen.AllCategories.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Live",
                icon = Icons.Default.Public,
                gradientColors = listOf(Color(0xFF4A90E2), Color(0xFF9013FE)),
                onClick = { onNavigate(Screen.LiveCategories.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Movies",
                icon = Icons.Default.Movie,
                gradientColors = listOf(Color(0xFFFF8C42), Color(0xFFFF3C5F)),
                onClick = { onNavigate(Screen.VodCategories.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Series",
                icon = Icons.Default.Tv,
                gradientColors = listOf(Color(0xFFF8D800), Color(0xFFF57F17)),
                onClick = { onNavigate(Screen.SeriesCategories.route) },
                modifier = Modifier.height(cardHeight)
            )
        }

        // --- SECOND ROW (12 columns total: 3 + 3 + 3 + 3) ---
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Favourite",
                icon = Icons.Default.Favorite,
                gradientColors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
                onClick = { onNavigate(Screen.Favorites.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Match Today",
                icon = Icons.Default.SportsSoccer,
                gradientColors = listOf(Color(0xFF1CB5E0), Color(0xFF000851)),
                onClick = { onNavigate(Screen.MatchToday.route) },
                modifier = Modifier.height(cardHeight)
            )
        }
        item(span = { GridItemSpan(3) }) {
            MatrixTileCard(
                title = "Profile",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                onClick = { onNavigate(Screen.ProfileFromHome.route) },
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
