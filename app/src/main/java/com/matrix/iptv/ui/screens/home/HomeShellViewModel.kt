package com.matrix.iptv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.domain.model.Profile
import com.matrix.iptv.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab { LIVE, VOD, SERIES, SEARCH, SETTINGS }

@HiltViewModel
class HomeShellViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _activeTab = MutableStateFlow(HomeTab.LIVE)
    val activeTab: StateFlow<HomeTab> = _activeTab

    val activeProfile: StateFlow<Profile?> = dataStoreManager.activeProfileId
        .flatMapLatest { id -> flow { emit(if (id.isEmpty()) null else profileRepository.getProfile(id)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectTab(tab: HomeTab) { _activeTab.value = tab }

    fun logout() = viewModelScope.launch {
        dataStoreManager.setActiveProfileId("")
    }
}
