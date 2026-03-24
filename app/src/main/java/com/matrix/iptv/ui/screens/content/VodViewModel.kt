package com.matrix.iptv.ui.screens.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.data.remote.model.VodCategory
import com.matrix.iptv.data.remote.model.VodStream
import com.matrix.iptv.data.remote.model.UiState
import com.matrix.iptv.domain.repository.ProfileRepository
import com.matrix.iptv.domain.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

enum class VodSort {
    DEFAULT,
    AZ,
    ZA,
    RATING,
    NEW
}

@HiltViewModel
class VodViewModel @Inject constructor(
    private val repository: XtreamRepository,
    private val dataStoreManager: DataStoreManager,
    private val profileRepository: ProfileRepository,
    private val favoriteRepository: com.matrix.iptv.domain.repository.FavoriteRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<UiState<List<VodCategory>>>(UiState.Idle)
    val categories: StateFlow<UiState<List<VodCategory>>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VodCategory?>(null)
    val selectedCategory: StateFlow<VodCategory?> = _selectedCategory.asStateFlow()

    private val _selectedMovie = MutableStateFlow<VodStream?>(null)
    val selectedMovie: StateFlow<VodStream?> = _selectedMovie.asStateFlow()

    private val _isSelectedMovieFavorite = MutableStateFlow(false)
    val isSelectedMovieFavorite = _isSelectedMovieFavorite.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

    private val rawStreams = MutableStateFlow<List<VodStream>>(emptyList())
    private val _streams = MutableStateFlow<UiState<List<VodStream>>>(UiState.Idle)
    val streams = _streams.asStateFlow()

    private val _sortOrder = MutableStateFlow(VodSort.DEFAULT)
    val sortOrder = _sortOrder.asStateFlow()

    private var cachedSession: Triple<String, String, String>? = null
    private var loadStreamsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            cachedSession = getSession()
            loadCategories()
        }
        observeSelectedMovie()
    }

    private fun observeSelectedMovie() {
        viewModelScope.launch {
            selectedMovie.collect { movie ->
                if (movie != null) {
                    _isSelectedMovieFavorite.value = favoriteRepository.isFavorite(movie.streamId.toString())
                }
            }
        }
    }

    fun toggleFavorite(movie: VodStream) {
        viewModelScope.launch {
            val fav = com.matrix.iptv.data.local.db.FavoriteEntity(
                streamId = movie.streamId.toString(),
                name = movie.name,
                type = "movie",
                icon = movie.icon,
                extension = movie.extension,
                categoryId = movie.categoryId
            )
            if (favoriteRepository.isFavorite(movie.streamId.toString())) {
                favoriteRepository.removeFavorite(fav)
                _isSelectedMovieFavorite.value = false
            } else {
                favoriteRepository.addFavorite(fav)
                _isSelectedMovieFavorite.value = true
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categories.value = UiState.Loading
            val session = cachedSession ?: getSession()?.also { cachedSession = it }
            if (session == null) {
                _categories.value = UiState.Error("No active profile")
                return@launch
            }
            repository.getVodCategories(session.first, session.second, session.third)
                .onSuccess { 
                    val list = listOf(VodCategory("-1", "ALL")) + it
                    _categories.value = UiState.Success(list)
                    if (list.isNotEmpty() && _selectedCategory.value == null) {
                        selectCategory(list.first())
                    }
                }
                .onFailure { _categories.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun selectCategory(category: VodCategory) {
        if (_selectedCategory.value?.id == category.id) return
        _selectedCategory.value = category
        loadStreams(category.id)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterStreams()
    }

    fun toggleSearchMode(enabled: Boolean) {
        _isSearchMode.value = enabled
        if (!enabled) {
            _searchQuery.value = ""
            filterStreams()
        }
    }

    fun updateSortOrder(sort: VodSort) {
        _sortOrder.value = sort
        filterStreams()
    }

    private var currentLimit = 100
    private var allFilteredAndSorted = emptyList<VodStream>()

    private fun filterStreams() {
        // Reset limit on every search/sort/category change
        currentLimit = 100
        val query = _searchQuery.value.trim()
        val sort = _sortOrder.value
        val all = rawStreams.value
        viewModelScope.launch {
            allFilteredAndSorted = withContext(Dispatchers.Default) {
                val filter = if (query.isEmpty()) all else all.filter { it.name.contains(query, ignoreCase = true) }
                when (sort) {
                    VodSort.DEFAULT -> filter
                    VodSort.AZ -> filter.sortedBy { it.name.lowercase() }
                    VodSort.ZA -> filter.sortedByDescending { it.name.lowercase() }
                    VodSort.RATING -> filter.sortedByDescending { it.rating?.toFloatOrNull() ?: 0f }
                    VodSort.NEW -> filter.sortedByDescending { it.added?.toLongOrNull() ?: 0L }
                }
            }
            _streams.value = UiState.Success(allFilteredAndSorted.take(currentLimit))
            _selectedMovie.value = allFilteredAndSorted.firstOrNull()
        }
    }

    fun loadMore() {
        if (currentLimit < allFilteredAndSorted.size) {
            currentLimit += 100
            _streams.value = UiState.Success(allFilteredAndSorted.take(currentLimit))
        }
    }

    fun selectMovie(movie: VodStream) {
        _selectedMovie.value = movie
    }

    /** Used by VodDetailsHostScreen when movie isn't in cache yet */
    fun requestMovieById(streamId: Int) {
        val found = rawStreams.value.find { it.streamId == streamId }
        if (found != null) {
            _selectedMovie.value = found
            return
        }
        
        // Not in RAM (e.g. from Home or Search) -> fetch from server directly!
        viewModelScope.launch {
            val session = cachedSession ?: getSession() ?: return@launch
            repository.getVodInfo(session.first, session.second, session.third, streamId)
                .onSuccess { infoRes ->
                    val data = infoRes.movieData
                    val info = infoRes.info
                    if (data != null) {
                        _selectedMovie.value = data.copy(
                            plot = info?.plot ?: data.plot,
                            plotAr = info?.plotAr ?: data.plotAr,
                            description = info?.description ?: data.description,
                            descriptionAr = info?.descriptionAr ?: data.descriptionAr,
                            icon = info?.icon ?: data.icon, // cover is sometimes mapped to icon
                            releaseDateSn = info?.releaseDateSn ?: data.releaseDateSn,
                            rating = info?.rating ?: data.rating
                        )
                    }
                }
        }
    }


    private fun loadStreams(categoryId: String) {
        loadStreamsJob?.cancel()
        loadStreamsJob = viewModelScope.launch {
            _streams.value = UiState.Loading
            val session = cachedSession ?: getSession()?.also { cachedSession = it }
            if (session == null) {
                _streams.value = UiState.Error("No active profile")
                return@launch
            }
            repository.getVodStreams(session.first, session.second, session.third, categoryId)
                .onSuccess { 
                    if (_selectedCategory.value?.id == categoryId) {
                        rawStreams.value = it
                        filterStreams()
                        _selectedMovie.value = it.firstOrNull()
                    }
                }
                .onFailure { 
                    if (_selectedCategory.value?.id == categoryId) {
                        _streams.value = UiState.Error(it.message ?: "Unknown error")
                    }
                }
        }
    }

    private suspend fun getSession(): Triple<String, String, String>? {
        val profileId = dataStoreManager.activeProfileId.first()
        if (profileId.isBlank()) return null
        val profile = profileRepository.getProfile(profileId) ?: return null
        val password = profileRepository.getPassword(profileId)
        return Triple(profile.host, profile.username, password)
    }
}
