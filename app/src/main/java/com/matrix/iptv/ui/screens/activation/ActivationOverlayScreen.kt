package com.matrix.iptv.ui.screens.activation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.BuildConfig
import com.matrix.iptv.data.remote.NetworkDiagnostic
import com.matrix.iptv.ui.components.BrandLogo
import com.matrix.iptv.ui.components.LoadingOverlay
import com.matrix.iptv.ui.theme.AccentOrange
import com.matrix.iptv.ui.theme.AccentPink
import com.matrix.iptv.ui.theme.matrixColors
import com.matrix.iptv.util.DeviceIdUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PillShape = RoundedCornerShape(50)

/**
 * ActivationOverlayScreen — skill_2.md §6.2
 *
 * Layout (landscape):
 *   Left card:  Logo + Trial status (days remaining / expired)
 *   Right card: Device ID + 3 action buttons
 *
 * Debug panel (DEBUG builds only): shown below the two cards.
 * Focus order: "Try App" (if enabled) → "Activate Now" → "Refresh"
 */
@Composable
fun ActivationOverlayScreen(
    onTryApp: () -> Unit,
    onActivateNow: () -> Unit,
    viewModel: ActivationOverlayViewModel = hiltViewModel()
) {
    val state   by viewModel.state.collectAsState()
    val mx      = MaterialTheme.matrixColors
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var idCopied by remember { mutableStateOf(false) }

    // Initial focus: Try App if trial valid, else Activate Now
    val tryAppFocus   = remember { FocusRequester() }
    val activateFocus = remember { FocusRequester() }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(100)
            if (state.isTrialValid) tryAppFocus.requestFocus()
            else activateFocus.requestFocus()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {

        if (state.isLoading) {
            LoadingOverlay()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 27.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Title row ─────────────────────────────────────────────────
                Text(
                    "Activation",
                    style = MaterialTheme.typography.headlineLarge,
                    color = mx.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Don't forget to activate before expiration.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = mx.textSecondary
                )
                Spacer(Modifier.height(20.dp))

                // ── Two side-by-side cards ────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // ── LEFT: Trial status ─────────────────────────────────────
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = mx.bgSurface),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            BrandLogo(size = 64.dp)
                            Spacer(Modifier.height(24.dp))

                            if (state.isTrialValid) {
                                Text(
                                    "Trial",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = mx.textSecondary
                                )
                                Text(
                                    "${state.trialDaysRemaining} Days Remaining",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = mx.accentPink,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = mx.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Trial Expired",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = mx.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Activation required to continue.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = mx.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ── RIGHT: Device ID + buttons ────────────────────────────
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = mx.bgSurface),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            // Device ID row
                            Text(
                                "MAC / ID:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = mx.textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = mx.bgElevated,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.deviceIdShort + if (BuildConfig.DEBUG) " (len=16)" else "",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = mx.accentPink,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        DeviceIdUtil.copyToClipboard(
                                            context, "Device ID", state.deviceIdShort
                                        )
                                        idCopied = true
                                        scope.launch { delay(2_000); idCopied = false }
                                    }) {
                                        Icon(
                                            imageVector = if (idCopied) Icons.Default.Check
                                                else Icons.Default.ContentCopy,
                                            contentDescription = "Copy ID",
                                            tint = if (idCopied) mx.success else mx.textSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(32.dp))

                            // ── 3 Buttons ─────────────────────────────────────

                            // 1) Try App (enabled only when trial valid)
                            Button(
                                onClick = onTryApp,
                                enabled = state.isTrialValid,
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor  = mx.accentPink,
                                    disabledContainerColor = mx.bgElevated
                                ),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(tryAppFocus)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Try App", style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(Modifier.height(12.dp))

                            // 2) Activate Now
                            OutlinedButton(
                                onClick = onActivateNow,
                                shape = PillShape,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, mx.accentPink),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = mx.accentPink),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(activateFocus)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Activate Now", style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(Modifier.height(12.dp))

                            // 3) Refresh — fires a real POST to backend
                            OutlinedButton(
                                onClick = { viewModel.load() },
                                shape = PillShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, mx.divider),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = mx.textSecondary),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Refresh", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── DEBUG panel (visible only in debug builds) ─────────────────
                if (BuildConfig.DEBUG) {
                    state.diagnostic?.let { diag ->
                        DebugNetworkPanel(diag)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Version — long-press DEBUG activation
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = mx.textMuted,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            if (BuildConfig.ENABLE_DEV_TOOLS) viewModel.debugActivate()
                        })
                    }
                )
            }
        }
    }
}

/** Compact on-screen debug panel showing full HTTP round-trip details */
@Composable
private fun DebugNetworkPanel(diag: NetworkDiagnostic) {
    val isError = diag.exceptionMessage != null || (diag.responseCode != null && diag.responseCode >= 400)
    val bgColor = if (isError) Color(0xFF2D0B0B) else Color(0xFF0B2D12)
    val labelColor = Color(0xFFBBBBBB)
    val valueColor = if (isError) Color(0xFFFF6B6B) else Color(0xFF6BFF8E)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                "🔍 DEBUG – Network Diagnostic",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFCC00),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            DebugRow("URL",     diag.url,    labelColor, valueColor)
            DebugRow("Method",  diag.method, labelColor, valueColor)
            DebugRow("Request", diag.requestBody, labelColor, valueColor)

            if (diag.responseCode != null) {
                DebugRow("HTTP",     "${diag.responseCode}", labelColor, valueColor)
                DebugRow("Response", diag.responseBody ?: "—", labelColor, valueColor)
            }

            if (diag.exceptionMessage != null) {
                DebugRow("Exception", diag.exceptionMessage, labelColor, Color(0xFFFF4444))
            }
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = labelColor,
            modifier = Modifier.widthIn(min = 70.dp)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}
