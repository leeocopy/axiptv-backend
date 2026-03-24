package com.matrix.iptv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.matrix.iptv.ui.navigation.Screen
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.draw.scale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import com.matrix.iptv.domain.model.FootballMatch

// ─── Shared Clock: one coroutine updates all composables ─────────────────────
val LocalClock = staticCompositionLocalOf { LocalDateTime.now() }

@Composable
fun ProvideSharedClock(content: @Composable () -> Unit) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000) // update every minute — sufficient for displayed time
            now = LocalDateTime.now()
        }
    }
    CompositionLocalProvider(LocalClock provides now, content = content)
}

@Composable
fun TvNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val ncx = Color(0xFF89E900)
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(Color.Black)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TvNavIcon(Icons.Default.Search, false) { onSearchClick() }
        
        Spacer(Modifier.height(16.dp))

        TvNavIcon(Icons.Default.Home, currentRoute == Screen.HomeDashboard.route) { onNavigate(Screen.HomeDashboard.route) }
        TvNavIcon(Icons.Default.LiveTv, currentRoute == Screen.LiveCategories.route || currentRoute == Screen.LiveStreams.route) { onNavigate(Screen.LiveCategories.route) }
        TvNavIcon(Icons.Default.Movie, currentRoute == Screen.VodCategories.route || currentRoute == Screen.VodStreams.route) { onNavigate(Screen.VodCategories.route) }
        TvNavIcon(Icons.Default.Tv, currentRoute == Screen.SeriesCategories.route || currentRoute == Screen.SeriesStreams.route) { onNavigate(Screen.SeriesCategories.route) }
        TvNavIcon(Icons.Default.Favorite, currentRoute == Screen.Favorites.route) { onNavigate(Screen.Favorites.route) }
        TvNavIcon(Icons.Default.SportsSoccer, currentRoute == Screen.MatchToday.route) { onNavigate(Screen.MatchToday.route) }
        
        Spacer(Modifier.weight(1f))
        
        TvNavIcon(Icons.Default.Settings, currentRoute == Screen.Settings.route) { onNavigate(Screen.Settings.route) }
    }
}

@Composable
fun TvNavIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isFocused) Color.White.copy(0.15f) else Color.Transparent,
        modifier = Modifier
            .size(48.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .focusable()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected || isFocused) Color(0xFF89E900) else Color.White.copy(0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MoviePosterCard(title: String, posterUrl: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.1f else 1f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(130.dp)
            .height(190.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .focusable()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(posterUrl)
                .crossfade(true)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MatchCard(match: FootballMatch) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)
    
    Surface(
        onClick = { /* Navigate to Match Details or Channel */ },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1D24),
        modifier = Modifier
            .width(280.dp)
            .height(140.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF89E900) else Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .focusable()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(match.competition, fontSize = 10.sp, color = Color.White.copy(0.5f), fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamLogo(match.homeLogo, match.homeTeam)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(match.timeInfo, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("KICK OFF", color = Color.White.copy(0.4f), fontSize = 8.sp)
                }
                TeamLogo(match.awayLogo, match.awayTeam)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(match.homeTeam, fontSize = 12.sp, color = Color.White)
                Text(match.awayTeam, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun TeamLogo(url: String, name: String) {
    AsyncImage(
        model = url,
        contentDescription = name,
        modifier = Modifier.size(40.dp)
    )
}

@Composable
fun TvRemoteHintRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TvHintItemIcon(Icons.Default.ArrowBack, "Navigate")
        TvHintItemText("OK", "Select")
        TvHintItemIcon(Icons.Default.Undo, "Back")
    }
}

@Composable
fun TvHintItemIcon(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
        Box(
            modifier = Modifier.size(20.dp).background(Color.White.copy(0.1f), CircleShape).border(0.5.dp, Color.White.copy(0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.White)
        }
        Spacer(Modifier.width(6.dp))
        Text(label.uppercase(), fontSize = 10.sp, color = Color.White.copy(0.6f))
    }
}

@Composable
fun TvHintItemText(key: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
        Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(4.dp)) {
            Text(key, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Text(label.uppercase(), fontSize = 10.sp, color = Color.White.copy(0.6f))
    }
}

@Composable
fun TvDashboardHeaderShort() {
    val currentTime = LocalClock.current
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd", Locale.ENGLISH)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(currentTime.format(timeFormatter), fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text(currentTime.format(dateFormatter), fontSize = 12.sp, color = Color.White.copy(0.6f))
        }
    }
}

@Composable
fun SearchInputOverlay(
    value: String,
    onValueChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Black.copy(0.9f))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.width(500.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Color(0xFF89E900), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            TvInput(
                value = value,
                onValueChange = onValueChange,
                hint = "Search here...",
                modifier = Modifier.weight(1f),
                focusRequester = focusRequester,
                onImeAction = onClose
            )
            Spacer(Modifier.width(16.dp))
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null, tint = Color.White.copy(0.6f))
            }
        }
    }
}
