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
import javax.inject.Inject

@HiltViewModel
class VodViewModel @Inject constructor(
    private val repository: XtreamRepository,
    private val dataStoreManager: DataStoreManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<UiState<List<VodCategory>>>(UiState.Idle)
    val categories: StateFlow<UiState<List<VodCategory>>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VodCategory?>(null)
    val selectedCategory: StateFlow<VodCategory?> = _selectedCategory.asStateFlow()

    private val _selectedMovie = MutableStateFlow<VodStream?>(null)
    val selectedMovie: StateFlow<VodStream?> = _selectedMovie.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

    private val rawStreams = MutableStateFlow<List<VodStream>>(emptyList())
    private val _streams = MutableStateFlow<UiState<List<VodStream>>>(UiState.Idle)
    val streams = _streams.asStateFlow()

    private val streamCache = mutableMapOf<String, List<VodStream>>()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categories.value = UiState.Loading
            val session = getSession() ?: return@launch run { _categories.value = UiState.Error("No active profile") }
            repository.getVodCategories(session.first, session.second, session.third)
                .onSuccess { 
                    val allCategory = VodCategory("-1", "ALL")
                    val list = listOf(allCategory) + it
                    _categories.value = UiState.Success(list)
                    if (list.isNotEmpty() && _selectedCategory.value == null) {
                        selectCategory(allCategory)
                    }
                }
                .onFailure { _categories.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun selectCategory(category: VodCategory) {
        _selectedCategory.value = category
        if (streamCache.containsKey(category.id)) {
            val list = streamCache[category.id]!!
            rawStreams.value = list
            filterStreams()
            _selectedMovie.value = list.firstOrNull()
        } else {
            loadStreams(category.id)
        }
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

    private fun filterStreams() {
        val query = _searchQuery.value.trim()
        val all = rawStreams.value
        if (query.isEmpty()) {
            _streams.value = UiState.Success(all)
        } else {
            val filtered = all.filter { it.name.contains(query, ignoreCase = true) }
            _streams.value = UiState.Success(filtered)
        }
    }

    fun selectMovie(movie: VodStream) {
        _selectedMovie.value = movie
    }

    private fun loadStreams(categoryId: String) {
        viewModelScope.launch {
            _streams.value = UiState.Loading
            val session = getSession() ?: return@launch run { _streams.value = UiState.Error("No active profile") }
            repository.getVodStreams(session.first, session.second, session.third, categoryId)
                .onSuccess { 
                    streamCache[categoryId] = it
                    if (_selectedCategory.value?.id == categoryId) {
                        rawStreams.value = it
                        filterStreams()
                        _selectedMovie.value = it.firstOrNull()
                    }
                }
                .onFailure { _streams.value = UiState.Error(it.message ?: "Unknown error") }
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
