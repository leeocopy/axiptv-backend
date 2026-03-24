package com.matrix.iptv.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.R
import com.matrix.iptv.ui.theme.matrixColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateProfiles: () -> Unit = {},
    onNavigatePro: () -> Unit = {},
    onNavigateSpeedTest: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val mx = MaterialTheme.matrixColors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val settingsItems = listOf(
        SettingItem(stringResource(R.string.settings_languages), Icons.Default.Language) { viewModel.showDialog(SettingsDialogType.Language) },
        SettingItem(stringResource(R.string.settings_parental), Icons.Default.Lock) { viewModel.showDialog(SettingsDialogType.ParentalControl) },
        SettingItem(stringResource(R.string.settings_automation), Icons.Default.Autorenew) { viewModel.showDialog(SettingsDialogType.Automation) },
        SettingItem("External Player", Icons.Default.PlayCircle) { viewModel.showDialog(SettingsDialogType.ExternalPlayer) },
        SettingItem(stringResource(R.string.settings_profiles), Icons.Default.Person) { onNavigateProfiles() },
        SettingItem(stringResource(R.string.settings_stream_format), Icons.Default.List) { viewModel.showDialog(SettingsDialogType.StreamFormat) },
        SettingItem(stringResource(R.string.settings_date_update), Icons.Default.Refresh) { viewModel.performDateUpdate(context) },
        SettingItem("Check for Updates", Icons.Default.SystemUpdate) { viewModel.checkAppUpdate() },
        SettingItem("App Theme", Icons.Default.Palette) { viewModel.showDialog(SettingsDialogType.Theme) },
        SettingItem(stringResource(R.string.settings_clear_cache), Icons.Default.Delete) { viewModel.showDialog(SettingsDialogType.ConfirmClearCache) },
        SettingItem(stringResource(R.string.settings_more_apps), Icons.Default.Apps) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:AXIPTV"))
            try { context.startActivity(i) } catch(e: Exception) {}
        },
        SettingItem(stringResource(R.string.settings_rate_us), Icons.Default.ThumbUp) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
            try { context.startActivity(i) } catch(e: Exception) {}
        },
        SettingItem(stringResource(R.string.settings_about), Icons.Default.Info) { viewModel.showDialog(SettingsDialogType.About) },
        SettingItem("Speed Test", Icons.Default.Speed) { onNavigateSpeedTest() },
        SettingItem(stringResource(R.string.settings_pro), Icons.Default.Star) { onNavigatePro() },
        SettingItem(stringResource(R.string.settings_logout), Icons.Default.ExitToApp) { viewModel.showDialog(SettingsDialogType.ConfirmLogout) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C111D))
            .padding(32.dp)
    ) {
        Column {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(settingsItems.size) { index ->
                    SettingsCard(settingsItems[index])
                }
            }
        }

        // --- Global Update Overlay ---
        if (state.updateConfig != null) {
            val config = state.updateConfig!!
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { if (!config.isCritical) viewModel.clearUpdateConfig() },
                title = { Text("Update Available", color = Color.White) },
                text = {
                    Column {
                        Text("Version ${config.latestVersion} is available.", color = Color.White.copy(0.7f))
                        Spacer(Modifier.height(8.dp))
                        Text(config.updateMessage, color = Color.White.copy(0.9f))
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                confirmButton = {
                    Button(
                        onClick = { uriHandler.openUri(config.updateUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = mx.accentHover)
                    ) {
                        Text("UPDATE NOW", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = if (!config.isCritical) {
                    {
                        TextButton(onClick = { viewModel.clearUpdateConfig() }) {
                            Text("LATER", color = Color.White.copy(0.5f))
                        }
                    }
                } else null
            )
        }
    }

    // --- Dialogs ---
    when (state.showDialog) {
        is SettingsDialogType.ConfirmClearCache -> {
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("Clear Cache") },
                text = { Text("Are you sure you want to clear all cached streams and categories? This may require re-downloading them next time.") },
                confirmButton = {
                    Button(onClick = { viewModel.clearCache(context) }) { Text("Clear") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        is SettingsDialogType.ConfirmLogout -> {
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to log out and switch to Profile Selection?") },
                confirmButton = {
                    Button(onClick = { 
                        viewModel.showDialog(null)
                        viewModel.logout(context, onLoggedOut = onNavigateProfiles)
                    }) { Text("Logout") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        is SettingsDialogType.About -> {
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("About AXIPTV") },
                text = { Text("AXIPTV v${com.matrix.iptv.BuildConfig.VERSION_NAME}\nBuilt for Android TV and Android Devices.\nEnjoy premium streaming experience.") },
                confirmButton = {
                    Button(onClick = { viewModel.showDialog(null) }) { Text("Close") }
                }
            )
        }
        is SettingsDialogType.Language -> {
            var selected by remember { mutableStateOf(state.language) }
            val options = listOf("en" to "English", "ar" to "العربية", "fr" to "Français")
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text(stringResource(R.string.settings_select_language)) },
                text = {
                    Column {
                        options.forEach { (code, name) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected == code, onClick = { selected = code })
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.setLanguage(selected) }) { Text(stringResource(R.string.settings_save)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text(stringResource(R.string.settings_cancel)) }
                }
            )
        }
        is SettingsDialogType.Theme -> {
            var selected by remember { mutableStateOf(state.appTheme) }
            val options = listOf("matrix" to "Matrix Green", "netflix" to "Netflix Red", "disney" to "Disney Blue", "prime" to "Prime Cyan")
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("App Theme") },
                text = {
                    Column {
                        options.forEach { (code, name) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected == code, onClick = { selected = code })
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.setAppTheme(selected) }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        is SettingsDialogType.StreamFormat -> {
            var selected by remember { mutableStateOf(state.streamFormat) }
            val options = listOf("ts" to "MPEG-TS (.ts)", "m3u8" to "HLS (.m3u8)")
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("Select Stream Format") },
                text = {
                    Column {
                        options.forEach { (code, name) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected == code, onClick = { selected = code })
                                Text(name)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("MPEG-TS is faster, HLS is better for unstable connections.", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.setStreamFormat(selected) }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        is SettingsDialogType.Automation -> {
            var auto by remember { mutableStateOf(state.autoStart) }
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("Automation") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = auto, onCheckedChange = { auto = it })
                        Text("Auto-start app on device boot")
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.setAutoStart(auto) }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        is SettingsDialogType.ExternalPlayer -> {
            var external by remember { mutableStateOf(state.externalPlayer) }
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("Playback Setting") },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = external, onCheckedChange = { external = it })
                            Text("Use External Player (VLC, MX)")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Recommended for buffering issues or specialized codecs.", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.setExternalPlayer(external) }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        is SettingsDialogType.ParentalControl -> {
            var pin by remember { mutableStateOf("") }
            var enabled by remember { mutableStateOf(state.parentalEnabled) }
            AlertDialog(
                onDismissRequest = { viewModel.showDialog(null) },
                title = { Text("Parental Control") },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = enabled, onCheckedChange = { enabled = it })
                            Spacer(Modifier.width(8.dp))
                            Text("Enable Parental Lock")
                        }
                        if (enabled) {
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if(it.length <= 4) pin = it },
                                label = { Text("4-digit PIN") },
                                singleLine = true
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        if (enabled && pin.length != 4) {
                            Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.setParentalEnabled(enabled, pin) 
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDialog(null) }) { Text("Cancel") }
                }
            )
        }
        null -> {}
    }
}

data class SettingItem(val title: String, val icon: ImageVector, val action: () -> Unit)

@Composable
fun SettingsCard(item: SettingItem) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        onClick = item.action,
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) Color.White.copy(0.12f) else Color.White.copy(0.05f),
        modifier = Modifier
            .height(130.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF89E900) else Color.White.copy(0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isFocused) Color(0xFF89E900) else Color.White.copy(0.7f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = item.title,
                color = if (isFocused) Color.White else Color.White.copy(0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

