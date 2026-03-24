package com.matrix.iptv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.data.remote.model.SeriesStream
import com.matrix.iptv.data.remote.model.VodStream
import com.matrix.iptv.domain.model.FootballMatch
import com.matrix.iptv.domain.repository.FootballRepository
import com.matrix.iptv.domain.repository.ProfileRepository
import com.matrix.iptv.domain.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllCategoriesUiState(
    val topMovies: List<VodStream> = emptyList(),
    val topSeries: List<SeriesStream> = emptyList(),
    val topArabicMovies: List<VodStream> = emptyList(),
    val topArabicSeries: List<SeriesStream> = emptyList(),
    val matchesToday: List<FootballMatch> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AllCategoriesViewModel @Inject constructor(
    private val repository: XtreamRepository,
    private val footballRepository: FootballRepository,
    private val dataStoreManager: DataStoreManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllCategoriesUiState())
    val uiState: StateFlow<AllCategoriesUiState> = _uiState.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    private var dataLoaded = false

    init {
        loadData()
        startMatchPolling()
    }

    private fun startMatchPolling() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                fetchMatches()
                kotlinx.coroutines.delay(15 * 60_000) // Refresh matches every 15 minutes (was 1 min — caused HTTP 429)
            }
        }
    }

    private suspend fun fetchMatches() {
        footballRepository.getMatchesToday().onSuccess { matches ->
            _uiState.update { it.copy(matchesToday = matches) }
        }.onFailure {
            android.util.Log.e("AllCategoriesVM", "Failed to fetch matches: ${it.message}")
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            if (dataLoaded) return@launch  // Already loaded — skip redundant 6 requests

            // 1. Loading active profile
            _uiState.update { it.copy(isLoading = true) }

            val profileId = dataStoreManager.activeProfileId.first()
            if (profileId.isEmpty()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            
            val profile = profileRepository.getProfile(profileId) ?: return@launch run { 
                _uiState.update { it.copy(isLoading = false) } 
            }
            val baseUrl = profile.host
            val user = profile.username
            val pass = profileRepository.getPassword(profileId)

            try {
                // 2. Fetch Categories (Fast)
                val vodCatsDeferred = async { repository.getVodCategories(baseUrl, user, pass).getOrDefault(emptyList()) }
                val seriesCatsDeferred = async { repository.getSeriesCategories(baseUrl, user, pass).getOrDefault(emptyList()) }

                val vodCats = vodCatsDeferred.await()
                val seriesCats = seriesCatsDeferred.await()

                // 3. Load Trending Content (Keywords: 2024, 2025, TRENDING, TOP, NEW)
                launch {
                    val trendingKeywords = listOf("2025", "2024", "TOP", "TRENDING", "NEW", "أحدث", "الأكثر")
                    val trendingCats = vodCats.filter { cat -> 
                        trendingKeywords.any { cat.name.contains(it, ignoreCase = true) }
                    }
                    
                    val list = mutableListOf<VodStream>()
                    // Use trending categories first, fallback to first categories
                    val targetCats = if (trendingCats.isNotEmpty()) trendingCats else vodCats.take(3)
                    
                    targetCats.take(3).forEach { cat ->
                        repository.getVodStreams(baseUrl, user, pass, cat.id).onSuccess { list.addAll(it) }
                    }
                    // Sort by newest if added date is available, otherwise take first
                    _uiState.update { it.copy(topMovies = list.distinctBy { m -> m.streamId }.take(20)) }
                }

                // Row: Arabic Movies
                launch {
                    val arabicVodCats = vodCats.filter { it.name.contains("ARABIC", true) || it.name.contains("ARABE", true) || it.name.contains("عربي", true) }
                    val list = mutableListOf<VodStream>()
                    arabicVodCats.take(2).forEach { cat ->
                        repository.getVodStreams(baseUrl, user, pass, cat.id).onSuccess { list.addAll(it) }
                    }
                    _uiState.update { it.copy(topArabicMovies = list.distinctBy { m -> m.streamId }.take(20)) }
                }

                // Row: Arabic Series (Trending/New)
                launch {
                    val arabicSeriesCats = seriesCats.filter { it.name.contains("ARABIC", true) || it.name.contains("ARABE", true) || it.name.contains("عربي", true) }
                    val list = mutableListOf<SeriesStream>()
                    // Get first 2 arabic categories (usually sorted by new in most servers)
                    arabicSeriesCats.take(2).forEach { cat ->
                        repository.getSeriesStreams(baseUrl, user, pass, cat.id).onSuccess { list.addAll(it) }
                    }
                    _uiState.update { it.copy(topArabicSeries = list.distinctBy { s -> s.seriesId }.take(20)) }
                }

                _uiState.update { it.copy(isLoading = false) }
                dataLoaded = true

            } catch (e: Exception) {
                android.util.Log.e("AllCategoriesVM", "Error loading", e)
                _uiState.update { it.copy(isLoading = false) }
                // dataLoaded stays false so retry is possible
            }
        }
    }

    override fun onCleared() {
        refreshJob?.cancel()
        super.onCleared()
    }
}
