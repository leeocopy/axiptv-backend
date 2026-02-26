package com.matrix.iptv.ui.screens.splash

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.BuildConfig
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.ui.theme.AccentOrange
import com.matrix.iptv.ui.theme.AccentPink
import com.matrix.iptv.ui.components.BrandLogo
import com.matrix.iptv.ui.theme.matrixColors
import com.matrix.iptv.domain.repository.DeviceStatusRepository
import com.matrix.iptv.domain.model.DeviceStatusReason
import com.matrix.iptv.util.DeviceIdUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Splash Result ─────────────────────────────────────────────────────────────
sealed class SplashResult {
    object Waiting : SplashResult()
    /** Fully activated — skip gate, go straight to content */
    data class Active(val hasProfile: Boolean) : SplashResult()
    /** Trial still valid — show activation overlay (Try App enabled) */
    object TrialValid : SplashResult()
    /** Trial expired and not activated — show activation overlay (Try App disabled) */
    object TrialExpired : SplashResult()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreManager: DataStoreManager,
    private val deviceStatusRepository: DeviceStatusRepository
) : ViewModel() {

    private val _result = MutableStateFlow<SplashResult>(SplashResult.Waiting)
    val result: StateFlow<SplashResult> = _result

    init { check() }

    private fun check() = viewModelScope.launch {
        kotlinx.coroutines.delay(1_400) // Min splash display time

        // 1) Compute + cache device id
        val full = DeviceIdUtil.computeFull(context)
        val short = DeviceIdUtil.short(full)
        dataStoreManager.setDeviceIdCached(full)
        dataStoreManager.setDeviceHash(short)

        android.util.Log.d("AXIPTV", "═══════════════════════════════════════")
        android.util.Log.d("AXIPTV", "SPLASH: firing device status check")
        android.util.Log.d("AXIPTV", "API_BASE_URL: ${BuildConfig.AXIPTV_BACKEND_URL}")
        android.util.Log.d("AXIPTV", "device_hash : $short")
        android.util.Log.d("AXIPTV", "═══════════════════════════════════════")

        // 2) Call remote status API — unconditionally, no isOnline gate
        val status = deviceStatusRepository.getStatus(short)

        val hasProfile = dataStoreManager.activeProfileId.first().isNotEmpty()

        // 3) Trial gate
        if (status.allowed) {
            if (status.reason == DeviceStatusReason.ACTIVE) {
                _result.value = SplashResult.Active(hasProfile)
            } else {
                _result.value = SplashResult.TrialValid
            }
        } else {
            _result.value = SplashResult.TrialExpired
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(
    /** Fully activated, go to content (hasProfile = whether active profile set) */
    onActive: (hasProfile: Boolean) -> Unit,
    /** Trial valid or expired — go to activation overlay */
    onShowActivation: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()

    LaunchedEffect(result) {
        when (val r = result) {
            is SplashResult.Active      -> onActive(r.hasProfile)
            is SplashResult.TrialValid,
            is SplashResult.TrialExpired -> onShowActivation()
            else -> Unit
        }
    }

    val mx = MaterialTheme.matrixColors
    val gradient = Brush.horizontalGradient(listOf(AccentPink, AccentOrange))
    val logoScale by animateFloatAsState(
        targetValue = 1f, animationSpec = tween(500, easing = EaseOut), label = "logo"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(mx.bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandLogo(size = 120.dp, modifier = Modifier.scale(logoScale))
            Spacer(Modifier.height(24.dp))
            Text("AXIPTV", style = MaterialTheme.typography.displayLarge, color = mx.textPrimary)
            Spacer(Modifier.height(32.dp))
            // 3-dot pulse loader
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    val dotAlpha by rememberInfiniteTransition(label = "d$i").animateFloat(
                        initialValue = 0.2f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            tween(600, delayMillis = i * 200), RepeatMode.Reverse
                        ), label = "dot$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                mx.accentPink.copy(alpha = dotAlpha),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = mx.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 27.dp)
        )
    }
}
