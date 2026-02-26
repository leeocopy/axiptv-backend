package com.matrix.iptv.ui.screens.activation

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.util.DeviceIdUtil
import com.matrix.iptv.util.QrUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PurchaseState(
    val deviceIdFull: String  = "",
    val deviceIdShort: String = "",
    val activationUrl: String = "",
    val qrBitmap: Bitmap?     = null,
    val isLoading: Boolean    = true
)

@HiltViewModel
class ActivatePurchaseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseState())
    val state: StateFlow<PurchaseState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }

        val full = dataStoreManager.deviceIdCached.first().ifEmpty {
            DeviceIdUtil.computeFull(context).also { dataStoreManager.setDeviceIdCached(it) }
        }
        val short = DeviceIdUtil.short(full)
        val url   = DeviceIdUtil.activationUrl(short)

        val qr = withContext(Dispatchers.Default) { QrUtil.generate(url, 512) }

        _state.value = PurchaseState(
            deviceIdFull  = full,
            deviceIdShort = short,
            activationUrl = url,
            qrBitmap      = qr,
            isLoading     = false
        )
    }
}
