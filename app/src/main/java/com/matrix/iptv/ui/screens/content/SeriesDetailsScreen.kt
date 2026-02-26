package com.matrix.iptv.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.data.remote.model.Episode
import com.matrix.iptv.data.remote.model.UiState
import com.matrix.iptv.ui.theme.FocusGlow
import com.matrix.iptv.ui.theme.matrixColors

@Composable
fun SeriesDetailsScreen(
    seriesId: Int,
    onEpisodeSelected: (String, String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val state by viewModel.seriesInfo.collectAsState()
    val mx = MaterialTheme.matrixColors
    
    var selectedSeasonKey by remember { mutableStateOf<String?>(null) }
    var focusedEpisode by remember { mutableStateOf<Episode?>(null) }

    LaunchedEffect(seriesId) {
        viewModel.loadSeriesInfo(seriesId)
    }

    // Set default season when data loaded
    LaunchedEffect(state) {
        if (state is UiState.Success && selectedSeasonKey == null) {
            val episodes = (state as UiState.Success).data.episodes
            if (!episodes.isNullOrEmpty()) {
                selectedSeasonKey = episodes.keys.first()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.White) }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = Color.Red) }
            is UiState.Success -> {
                val seriesInfo = s.data
                val seasonEpisodes = seriesInfo.episodes ?: emptyMap()
                val currentEpisodes = seasonEpisodes[selectedSeasonKey] ?: emptyList()

                MatrixBrowseLayout(
                    left = {
                        Text(
                            "Seasons", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(seasonEpisodes.keys.toList()) { key ->
                                val isSelected = key == selectedSeasonKey
                                var isFocused by remember { mutableStateOf(false) }
                                
                                Surface(
                                    onClick = { selectedSeasonKey = key },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { isFocused = it.isFocused },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isFocused) mx.accentPink else if (isSelected) mx.accentPink.copy(alpha = 0.2f) else Color.Transparent
                                ) {
                                    Text(
                                        text = "Season $key",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isFocused) Color.White else if (isSelected) mx.accentPink else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    },
                    center = {
                        ContentHeader(seriesInfo.info?.name ?: "Episodes", onBack)
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentEpisodes) { episode ->
                                EpisodeListItem(
                                    episode = episode,
                                    onClick = {
                                        onEpisodeSelected(
                                            episode.id ?: "", 
                                            episode.title ?: "Episode ${episode.num}", 
                                            episode.container_extension ?: "mp4"
                                        )
                                    },
                                    onFocused = { focusedEpisode = episode }
                                )
                            }
                        }
                    },
                    right = {
                        val displayEpisode = focusedEpisode
                        val desc = displayEpisode?.descriptionAr ?: displayEpisode?.plotAr ?: displayEpisode?.plot 
                            ?: seriesInfo.info?.plot ?: "No additional information available for this episode."
                        
                        MediaDetailsPanel(
                            title = displayEpisode?.title ?: seriesInfo.info?.name ?: "Details",
                            description = desc,
                            metadata = if (displayEpisode != null) "S${selectedSeasonKey} E${displayEpisode.num}" else "Series Details",
                            posterUrl = seriesInfo.info?.cover,
                            backdropUrl = seriesInfo.info?.cover
                        )
                    }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun EpisodeListItem(
    episode: Episode,
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val mx = MaterialTheme.matrixColors
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { 
                isFocused = it.isFocused
                if (it.isFocused) onFocused()
            },
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isFocused) androidx.compose.foundation.BorderStroke(1.dp, FocusGlow) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${episode.num}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isFocused) FocusGlow else Color.White
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title ?: "Episode ${episode.num}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Episode • ${episode.container_extension?.uppercase() ?: "MP4"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            
            if (isFocused) {
                Icon(
                    Icons.Default.PlayArrow,
                    null,
                    tint = mx.accentPink,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
