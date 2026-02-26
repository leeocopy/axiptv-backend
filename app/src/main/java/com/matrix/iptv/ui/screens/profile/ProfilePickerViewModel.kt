package com.matrix.iptv.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.domain.model.Profile
import com.matrix.iptv.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilePickerViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val profiles: StateFlow<List<Profile>> = repository.observeProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProfile(profile: Profile, onDone: () -> Unit) = viewModelScope.launch {
        dataStoreManager.setActiveProfileId(profile.id)
        onDone()
    }

    fun deleteProfile(id: String) = viewModelScope.launch {
        repository.deleteProfile(id)
    }
}
