package com.matrix.iptv.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matrix.iptv.ui.navigation.Screen
import com.matrix.iptv.ui.screens.content.*

@Composable
fun HomeShellScreen(
    onSwitchProfile: () -> Unit,
    onPlayContent: (contentId: String, type: String, title: String, ext: String) -> Unit,
    onNavigateRoot: (String) -> Unit,
    viewModel: HomeShellViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.HomeDashboard.route
    ) {
        composable(Screen.HomeDashboard.route) {
            HomeDashboardScreen(
                onNavigate = { route ->
                    if (route == Screen.Settings.route) {
                        onNavigateRoot(route)
                    } else {
                        navController.navigate(route)
                    }
                }
            )
        }

        // --- LIVE SECTION ---
        composable(Screen.LiveCategories.route) {
            LiveBrowseScreen(
                onPlayChannel = { streamId, name ->
                    onPlayContent(streamId, "live", name, "ts")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // --- VOD SECTION ---
        composable(Screen.VodCategories.route) {
            VodBrowseScreen(
                onPlayMovie = { streamId, ext ->
                    onPlayContent(streamId, "movie", "Movie", ext)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // --- SERIES SECTION ---
        composable(Screen.SeriesCategories.route) {
            SeriesBrowseScreen(
                onSeriesSelected = { seriesId ->
                    navController.navigate(Screen.SeriesDetails.createRoute(seriesId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SeriesDetails.route) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString(Screen.SeriesDetails.ARG_SERIES_ID)?.toIntOrNull() ?: 0
            SeriesDetailsScreen(
                seriesId = seriesId,
                onEpisodeSelected = { episodeId, title, ext ->
                    onPlayContent(episodeId, "series", title, ext)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // --- OTHERS ---
        composable(Screen.Favorites.route) {
            CategoryPlaceholderScreen("Favorites") { navController.popBackStack() }
        }
        composable(Screen.Music.route) {
            CategoryPlaceholderScreen("Music") { navController.popBackStack() }
        }
        composable(Screen.Entertainment.route) {
            CategoryPlaceholderScreen("Entertainment") { navController.popBackStack() }
        }
        composable(Screen.Radio.route) {
            CategoryPlaceholderScreen("Radio") { navController.popBackStack() }
        }
        composable(Screen.Devotional.route) {
            CategoryPlaceholderScreen("Devotional") { navController.popBackStack() }
        }
    }
}
