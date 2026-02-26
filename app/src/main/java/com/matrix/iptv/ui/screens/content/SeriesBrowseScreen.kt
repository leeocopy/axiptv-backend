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
fun SeriesBrowseScreen(
    onSeriesSelected: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val categoryState by viewModel.categories.collectAsState()
    val streamState by viewModel.streams.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSeries by viewModel.selectedSeries.collectAsState()
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
                text = selectedCategory?.name ?: "Series",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when (val s = streamState) {
                is UiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.data) { series ->
                            ContentCardWide(
                                title = series.name,
                                subtitle = "Genre: ${series.genre ?: "N/A"} • Rating: ${series.rating ?: "N/A"}",
                                imageUrl = series.icon,
                                badges = listOfNotNull(series.rating, "Series"),
                                onClick = { onSeriesSelected(series.seriesId) },
                                modifier = Modifier.onFocusChanged { if (it.isFocused) viewModel.selectSeries(series) }
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
            selectedSeries?.let { series ->
                val desc = series.descriptionAr ?: series.plotAr ?: series.plot ?: "No plot information available for this series. Select to view seasons and episodes."
                
                MediaDetailsPanel(
                    title = series.name,
                    description = desc,
                    metadata = "Series • ${selectedCategory?.name ?: ""} • ${series.rating ?: "8.2"}/10",
                    posterUrl = series.icon,
                    backdropUrl = series.icon
                )
            } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Select a series to see details", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
