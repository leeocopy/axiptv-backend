package com.matrix.iptv

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.matrix.iptv.ui.navigation.AppNavGraph
import com.matrix.iptv.ui.theme.MatrixIptvTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var dataStoreManager: com.matrix.iptv.data.local.prefs.DataStoreManager

    @javax.inject.Inject
    lateinit var profileRepository: com.matrix.iptv.domain.repository.ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Double-lock landscape — Manifest + runtime
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            MatrixIptvTheme {
                AppNavGraph(
                    dataStoreManager = dataStoreManager,
                    profileRepository = profileRepository,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}
