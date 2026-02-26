package com.matrix.iptv.ui.screens.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    contentId: String,
    type: String,
    title: String,
    extension: String,
    onBack: () -> Unit,
    dataStoreManager: DataStoreManager,
    profileRepository: ProfileRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isLoading = state == Player.STATE_BUFFERING
                    if (state == Player.STATE_READY) isPlaying = true
                }

                override fun onPlayerError(error: PlaybackException) {
                    errorMessage = "Playback error: ${error.localizedMessage ?: "Unknown error"}. Check server or format."
                    isLoading = false
                }
            })
        }
    }

    DisposableEffect(exoPlayer) {
        player = exoPlayer
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(contentId) {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            val profileId = dataStoreManager.activeProfileId.first()
            if (profileId.isBlank()) {
                errorMessage = "No active profile session found."
                isLoading = false
                return@launch
            }
            
            val profile = profileRepository.getProfile(profileId)
            val password = profileRepository.getPassword(profileId)
            
            if (profile == null) {
                errorMessage = "Active profile details could not be loaded."
                isLoading = false
                return@launch
            }

            // Build Stream URL based on type
            val streamUrl = when (type.lowercase()) {
                "live" -> "${profile.host}/live/${profile.username}/$password/$contentId.m3u8"
                "movie" -> {
                    val ext = if (extension.isBlank() || extension == "null") "mp4" else extension
                    "${profile.host}/movie/${profile.username}/$password/$contentId.$ext"
                }
                "series" -> {
                    val ext = if (extension.isBlank() || extension == "null") "mp4" else extension
                    "${profile.host}/series/${profile.username}/$password/$contentId.$ext"
                }
                else -> null
            }

            if (streamUrl == null) {
                errorMessage = "Invalid stream type: $type"
                isLoading = false
                return@launch
            }

            val mediaItem = MediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    this.player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Navigation
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Exit Player", tint = Color.White)
        }

        // Title Info Overlay
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp).background(Color.Black.copy(alpha = 0.4f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(text = "${type.uppercase()} STREAM", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }

        // Loading Overlay
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Error Banner
        errorMessage?.let { msg ->
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                color = Color.Red.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Text(text = msg, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Button(
                        onClick = { onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}
