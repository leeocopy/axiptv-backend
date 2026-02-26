package com.matrix.iptv.ui.screens.activation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.BuildConfig
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.data.remote.DiagnosticsStore
import com.matrix.iptv.data.remote.NetworkDiagnostic
import com.matrix.iptv.domain.repository.DeviceStatusRepository
import com.matrix.iptv.domain.model.DeviceStatusReason
import com.matrix.iptv.util.DeviceIdUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivationOverlayState(
    val deviceIdShort: String   = "",
    val deviceIdFull: String    = "",
    val trialDaysRemaining: Int = 0,
    val isTrialValid: Boolean   = false,
    val isActivated: Boolean    = false,
    val isLoading: Boolean      = true,
    val diagnostic: NetworkDiagnostic? = null
)

@HiltViewModel
class ActivationOverlayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreManager: DataStoreManager,
    private val deviceStatusRepository: DeviceStatusRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ActivationOverlayState())
    val state: StateFlow<ActivationOverlayState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }

        val full  = dataStoreManager.deviceIdCached.first().ifEmpty {
            DeviceIdUtil.computeFull(context).also { dataStoreManager.setDeviceIdCached(it) }
        }
        val short = DeviceIdUtil.short(full)

        // Always hits the network — no isOnline gate
        val status = deviceStatusRepository.getStatus(short)

        // Pick up diagnostics written by RemoteDeviceStatusService
        val diag = DiagnosticsStore.last

        val trialValid = status.allowed && (status.reason == DeviceStatusReason.TRIAL_ACTIVE || status.reason == DeviceStatusReason.ACTIVE)
        val paidActive = status.allowed && status.reason == DeviceStatusReason.ACTIVE

        _state.value = ActivationOverlayState(
            deviceIdShort      = short,
            deviceIdFull       = full,
            trialDaysRemaining = status.trialDaysRemaining,
            isTrialValid       = trialValid,
            isActivated        = paidActive,
            isLoading          = false,
            diagnostic         = diag
        )
    }

    /** DEBUG only — simulates paid activation */
    fun debugActivate() = viewModelScope.launch {
        if (BuildConfig.ENABLE_DEV_TOOLS) {
            dataStoreManager.debugActivate()
            load()
        }
    }
}
