package com.matrix.iptv.ui.screens.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.data.remote.model.UiState

@Composable
fun LiveBrowseScreen(
    onPlayChannel: (String, String) -> Unit, // Updated to pass stream name or more
    onBack: () -> Unit,
    viewModel: LiveViewModel = hiltViewModel()
) {
    val categoryState by viewModel.categories.collectAsState()
    val streamState by viewModel.streams.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedStream by viewModel.selectedStream.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchMode by viewModel.isSearchMode.collectAsState()

    MatrixBrowseLayout(
        left = {
            when (val s = categoryState) {
                is UiState.Success -> {
                    CategoriesSidebar(
                        categories = s.data.map { it.name },
                        selectedCategory = selectedCategory?.name ?: "",
                        onCategorySelected = { name ->
                            s.data.find { it.name == name }?.let { viewModel.selectCategory(it) }
                        },
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                        isSearchMode = isSearchMode,
                        onSearchToggle = { viewModel.toggleSearchMode(it) }
                    )
                }
                is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.White) }
                else -> {}
            }
        },
        center = {
            Text(
                text = selectedCategory?.name ?: "Channels",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when (val s = streamState) {
                is UiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data) { stream ->
                            ContentCardWide(
                                title = stream.name,
                                subtitle = "Channel ID: ${stream.streamId}",
                                imageUrl = stream.icon,
                                badges = listOfNotNull(if (stream.epgId != null) "EPG" else null, "HD"),
                                onClick = { onPlayChannel(stream.streamId.toString(), stream.name ?: "Live Channel") },
                                modifier = Modifier.onFocusChanged { if (it.isFocused) viewModel.selectStream(stream) }
                            )
                        }
                    }
                }
                is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.White) }
                is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message, color = Color.Red) }
                else -> {}
            }
        },
        right = {
            selectedStream?.let { stream ->
                DetailsPanel(
                    title = stream.name,
                    description = "Channel is ready for streaming. Select to start player. EPG information will be available soon.",
                    metadata = "Live TV • ${selectedCategory?.name ?: ""}",
                    coverUrl = stream.icon
                )
            } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Select a channel to see details", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
