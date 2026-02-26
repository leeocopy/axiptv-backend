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
fun VodBrowseScreen(
    onPlayMovie: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: VodViewModel = hiltViewModel()
) {
    val categoryState by viewModel.categories.collectAsState()
    val streamState by viewModel.streams.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedMovie by viewModel.selectedMovie.collectAsState()
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
                text = selectedCategory?.name ?: "Movies",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when (val s = streamState) {
                is UiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data) { movie ->
                            ContentCardWide(
                                title = movie.name,
                                subtitle = "Rating: ${movie.rating ?: "N/A"} • Year: ${movie.added?.take(4) ?: "N/A"}",
                                imageUrl = movie.icon,
                                badges = listOfNotNull(movie.extension?.uppercase(), "4K"),
                                onClick = { onPlayMovie(movie.streamId.toString(), movie.extension ?: "mp4") },
                                modifier = Modifier.onFocusChanged { if (it.isFocused) viewModel.selectMovie(movie) }
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
            selectedMovie?.let { movie ->
                // Priority: description_ar / plot_ar -> plot -> fallback
                val desc = movie.descriptionAr ?: movie.plotAr ?: movie.plot ?: "No plot information available for this movie. Enjoy the high-quality 4K stream."
                
                MediaDetailsPanel(
                    title = movie.name,
                    description = desc,
                    metadata = "Movie • ${selectedCategory?.name ?: ""} • ${movie.rating ?: "7.5"}/10",
                    posterUrl = movie.icon,
                    backdropUrl = movie.icon
                )
            } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Select a movie to see details", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
