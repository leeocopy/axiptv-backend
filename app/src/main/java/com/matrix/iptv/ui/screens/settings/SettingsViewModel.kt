package com.matrix.iptv.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.data.local.db.StreamCacheDao
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.data.local.prefs.SecurePrefs
import com.matrix.iptv.domain.repository.FootballRepository
import com.matrix.iptv.data.repository.FootballRepositoryImpl
import com.matrix.iptv.domain.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import coil.imageLoader
import android.content.Context
import javax.inject.Inject

data class SettingsUiState(
    val language: String = "en",
    val streamFormat: String = "ts",
    val parentalEnabled: Boolean = false,
    val autoStart: Boolean = false,
    val externalPlayer: Boolean = false,
    val appTheme: String = "matrix",
    val showDialog: SettingsDialogType? = null,
    val message: String? = null,
    val isClearingCache: Boolean = false,
    val updateConfig: com.matrix.iptv.data.remote.AppConfigResponse? = null
)

sealed class SettingsDialogType {
    object Language : SettingsDialogType()
    object StreamFormat : SettingsDialogType()
    object ParentalControl : SettingsDialogType()
    object Automation : SettingsDialogType()
    object ExternalPlayer : SettingsDialogType()
    object Theme : SettingsDialogType()
    object ConfirmClearCache : SettingsDialogType()
    object ConfirmLogout : SettingsDialogType()
    object About : SettingsDialogType()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val securePrefs: SecurePrefs,
    private val xtreamRepository: XtreamRepository,
    private val streamCacheDao: StreamCacheDao,
    private val footballRepository: FootballRepository,
    private val appConfigRepository: com.matrix.iptv.data.repository.AppConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val lang = dataStoreManager.language.first()
            val format = dataStoreManager.streamFormat.first()
            val parental = dataStoreManager.parentalEnabled.first()
            val autoStart = dataStoreManager.autoStart.first()
            val external = dataStoreManager.externalPlayer.first()
            val theme = dataStoreManager.appTheme.first()
            
            _state.update { 
                it.copy(
                    language = lang,
                    streamFormat = format,
                    parentalEnabled = parental,
                    autoStart = autoStart,
                    externalPlayer = external,
                    appTheme = theme
                )
            }
        }
    }

    fun showDialog(type: SettingsDialogType?) {
        _state.update { it.copy(showDialog = type) }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    // --- Settings Actions ---

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            dataStoreManager.setLanguage(lang)
            _state.update { it.copy(language = lang, showDialog = null, message = "Language updated to $lang") }
        }
    }

    fun setStreamFormat(format: String) {
        viewModelScope.launch {
            dataStoreManager.setStreamFormat(format)
            _state.update { it.copy(streamFormat = format, showDialog = null, message = "Stream format updated to $format") }
        }
    }

    fun setParentalEnabled(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            securePrefs.saveParentalPin(pin)
            dataStoreManager.setParentalEnabled(enabled)
            _state.update { it.copy(parentalEnabled = enabled, showDialog = null, message = "Parental control updated") }
        }
    }
    
    fun verifyCurrentPin(pin: String): Boolean {
        val currentPin = securePrefs.getParentalPin()
        return currentPin == pin || currentPin == null
    }

    fun setAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setAutoStart(enabled)
            _state.update { it.copy(autoStart = enabled, showDialog = null, message = "Auto Start updated") }
        }
    }

    fun setExternalPlayer(external: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setExternalPlayer(external)
            _state.update { it.copy(externalPlayer = external, showDialog = null, message = "Playback setting updated") }
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            dataStoreManager.setAppTheme(theme)
            _state.update { it.copy(appTheme = theme, showDialog = null, message = "App Theme updated to $theme. It may take a moment to apply.") }
        }
    }

    fun clearCache(context: Context? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isClearingCache = true, showDialog = null) }
            
            // Clear API & Image Cache
            xtreamRepository.clearCache()
            
            context?.let { ctx ->
                ctx.imageLoader.memoryCache?.clear()
                ctx.imageLoader.diskCache?.clear()
            }
            
            // Clear Match Cache
            if (footballRepository is FootballRepositoryImpl) {
                footballRepository.clearMatchCache()
            }
            
            // Clear Room DB
            streamCacheDao.clearAllStreams()
            streamCacheDao.clearAllCategories()
            
            _state.update { it.copy(isClearingCache = false, message = "Cache cleared successfully") }
        }
    }

    fun performDateUpdate(context: Context? = null) {
        // Essentially same as clearCache but might trigger reload in future
        clearCache(context)
    }

    fun checkAppUpdate() {
        viewModelScope.launch {
            appConfigRepository.getAppConfig().onSuccess { config ->
                if (config.latestVersionCode > com.matrix.iptv.BuildConfig.VERSION_CODE) {
                    _state.update { it.copy(updateConfig = config) }
                } else {
                    _state.update { it.copy(message = "App is up to date (v${com.matrix.iptv.BuildConfig.VERSION_NAME})") }
                }
            }.onFailure {
                _state.update { it.copy(message = "Failed to check for updates") }
            }
        }
    }

    fun clearUpdateConfig() {
        _state.update { it.copy(updateConfig = null) }
    }

    fun logout(context: Context? = null, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            clearCache(context)
            
            // Optional: You could wipe the active profile to force re-selection
            // But usually users just want to be sent back to profile picker
            onLoggedOut()
        }
    }
}
