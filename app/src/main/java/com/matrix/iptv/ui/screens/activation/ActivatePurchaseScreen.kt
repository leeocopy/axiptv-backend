package com.matrix.iptv.ui.screens.activation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.ui.components.LoadingOverlay
import com.matrix.iptv.ui.theme.matrixColors
import com.matrix.iptv.util.DeviceIdUtil
import com.matrix.iptv.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PillShape = RoundedCornerShape(50)

/**
 * ActivatePurchaseScreen — skill_2.md §6.2
 *
 * Landscape layout:
 *  Left Card  → QR code (generated locally with ZXing)
 *  Right Card → Device ID + Activation URL (copy + open) + Refresh + Back buttons
 */
@Composable
fun ActivatePurchaseScreen(
    onBack: () -> Unit,
    viewModel: ActivatePurchaseViewModel = hiltViewModel()
) {
    val state   by viewModel.state.collectAsState()
    val mx      = MaterialTheme.matrixColors
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var idCopied  by remember { mutableStateOf(false) }
    var urlCopied by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {
        if (state.isLoading) {
            LoadingOverlay()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 27.dp)
            ) {

                // ── Title ─────────────────────────────────────────────────────
                Text(
                    "Activate / Purchase",
                    style = MaterialTheme.typography.headlineLarge,
                    color = mx.textPrimary
                )
                Spacer(Modifier.height(20.dp))

                // ── Two cards ─────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // ─ LEFT: QR Code ──────────────────────────────────────────
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = mx.bgSurface),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = state.qrBitmap
                            if (bmp != null) {
                                Surface(
                                    color = androidx.compose.ui.graphics.Color.White,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.size(220.dp)
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Activation QR code",
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = mx.textMuted,
                                    modifier = Modifier.size(120.dp)
                                )
                            }
                        }
                    }

                    // ─ RIGHT: Details + buttons ───────────────────────────────
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = mx.bgSurface),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(36.dp),
                            verticalArrangement = Arrangement.Center
                        ) {

                            // ── Device ID ─────────────────────────────────────
                            Text(
                                "Device ID:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = mx.textSecondary
                            )
                            Spacer(Modifier.height(6.dp))
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
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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

                            Spacer(Modifier.height(20.dp))

                            // ── Activation URL ────────────────────────────────
                            Text(
                                "Or visit this link:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = mx.textSecondary
                            )
                            Spacer(Modifier.height(6.dp))
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
                                        text = state.activationUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        ),
                                        color = mx.accentPink,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2
                                    )
                                    // Copy URL
                                    IconButton(onClick = {
                                        DeviceIdUtil.copyToClipboard(
                                            context, "Activation URL", state.activationUrl
                                        )
                                        urlCopied = true
                                        scope.launch { delay(2_000); urlCopied = false }
                                    }) {
                                        Icon(
                                            imageVector = if (urlCopied) Icons.Default.Check
                                                else Icons.Default.ContentCopy,
                                            contentDescription = "Copy URL",
                                            tint = if (urlCopied) mx.success else mx.textSecondary
                                        )
                                    }
                                    // Open in browser
                                    IconButton(onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(state.activationUrl)
                                        )
                                        context.startActivity(intent)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = "Open link",
                                            tint = mx.textSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            // ── Buttons ───────────────────────────────────────
                            // Refresh Status
                            // TODO: call real backend API to check activation status
                            Button(
                                onClick = { viewModel.load() },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = mx.accentPink),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Refresh Status", style = MaterialTheme.typography.labelLarge)
                            }

                            Spacer(Modifier.height(12.dp))

                            // Back
                            OutlinedButton(
                                onClick = onBack,
                                shape = PillShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, mx.divider),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = mx.textSecondary),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Back", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
