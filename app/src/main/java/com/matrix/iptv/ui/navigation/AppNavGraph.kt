package com.matrix.iptv.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.util.PlaybackHelper
import com.matrix.iptv.domain.repository.ProfileRepository
import com.matrix.iptv.ui.components.ProvideSharedClock
import com.matrix.iptv.ui.screens.activation.ActivatePurchaseScreen
import com.matrix.iptv.ui.screens.activation.ActivationOverlayScreen
import com.matrix.iptv.ui.screens.home.HomeShellScreen
import com.matrix.iptv.ui.screens.player.PlayerScreen
import com.matrix.iptv.ui.screens.profile.ProfileAddEditScreen
import com.matrix.iptv.ui.screens.profile.ProfilePickerScreen
import com.matrix.iptv.ui.screens.settings.SettingsScreen
import com.matrix.iptv.ui.screens.speedtest.SpeedTestScreen
import com.matrix.iptv.ui.screens.splash.SplashScreen

@Composable
fun AppNavGraph(
    dataStoreManager: DataStoreManager,
    profileRepository: ProfileRepository,
    playbackHelper: PlaybackHelper,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ProvideSharedClock {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        modifier         = modifier,
        enterTransition  = {
            fadeIn(animationSpec = tween(220)) +
            slideInHorizontally(animationSpec = tween(220)) { it / 12 }
        },
        exitTransition   = {
            fadeOut(animationSpec = tween(180)) +
            slideOutHorizontally(animationSpec = tween(180)) { -it / 12 }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) +
            slideInHorizontally(animationSpec = tween(220)) { -it / 12 }
        },
        popExitTransition  = {
            fadeOut(animationSpec = tween(180)) +
            slideOutHorizontally(animationSpec = tween(180)) { it / 12 }
        }
    ) {

        // ── Splash ──────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onActive = { hasProfile ->
                    val dest = if (hasProfile) Screen.HomeShell.route
                               else Screen.ProfilePicker.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onShowActivation = {
                    navController.navigate(Screen.ActivationOverlay.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Activation Overlay (trial gate) ─────────────────────────────────
        composable(Screen.ActivationOverlay.route) {
            ActivationOverlayScreen(
                onTryApp = {
                    navController.navigate(Screen.ProfilePicker.route) {
                        popUpTo(Screen.ActivationOverlay.route) { inclusive = false }
                    }
                },
                onActivateNow = {
                    navController.navigate(Screen.ActivatePurchase.route)
                }
            )
        }

        // ── Activate / Purchase page ─────────────────────────────────────────
        composable(Screen.ActivatePurchase.route) {
            ActivatePurchaseScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Profile Picker ───────────────────────────────────────────────────
        composable(Screen.ProfilePicker.route) {
            ProfilePickerScreen(
                onProfileSelected = { _ ->
                    navController.navigate(Screen.HomeShell.route) {
                        popUpTo(Screen.ProfilePicker.route) { inclusive = false }
                    }
                },
                onAddProfile  = { navController.navigate(Screen.ProfileAdd.route) },
                onEditProfile = { id -> navController.navigate(Screen.ProfileEdit.createRoute(id)) },
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Profile Add ──────────────────────────────────────────────────────
        composable(Screen.ProfileAdd.route) {
            ProfileAddEditScreen(
                profileId = null,
                onSaved   = { navController.popBackStack() },
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Profile Edit ─────────────────────────────────────────────────────
        composable(
            route     = Screen.ProfileEdit.route,
            arguments = listOf(navArgument(Screen.ProfileEdit.ARG) { type = NavType.StringType })
        ) { back ->
            ProfileAddEditScreen(
                profileId = back.arguments?.getString(Screen.ProfileEdit.ARG),
                onSaved   = { navController.popBackStack() },
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Home Shell Graph ────────────────────────────────────────────────
        composable(Screen.HomeShell.route) {
            HomeShellScreen(
                onSwitchProfile = {
                    navController.navigate(Screen.ProfilePicker.route) {
                        popUpTo(Screen.HomeShell.route) { inclusive = false }
                    }
                },
                onPlayContent = { contentId, type, title, ext, catId ->
                    scope.launch {
                        playbackHelper.playStream(context, contentId, type, ext) {
                            navController.navigate(Screen.Player.createRoute(contentId, type, title, ext, catId))
                        }
                    }
                },
                onNavigateRoot = { route ->
                    navController.navigate(route)
                }
            )
        }

        // ── Settings ────────────────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateProfiles = {
                    navController.navigate(Screen.ProfilePicker.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigatePro = { navController.navigate(Screen.ActivatePurchase.route) },
                onNavigateSpeedTest = { navController.navigate(Screen.SpeedTest.route) }
            )
        }

        // ── Speed Test ──────────────────────────────────────────────────────
        composable(Screen.SpeedTest.route) {
            SpeedTestScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Player ───────────────────────────────────────────────────────────
        composable(
            route     = Screen.Player.route,
            arguments = listOf(
                navArgument(Screen.Player.ARG_CONTENT_ID) { type = NavType.StringType },
                navArgument(Screen.Player.ARG_TYPE)       { type = NavType.StringType },
                navArgument(Screen.Player.ARG_TITLE)      { type = NavType.StringType },
                navArgument(Screen.Player.ARG_EXTENSION)  { type = NavType.StringType },
                navArgument(Screen.Player.ARG_CATEGORY_ID) { type = NavType.StringType }
            )
        ) { back ->
            PlayerScreen(
                contentId = back.arguments?.getString(Screen.Player.ARG_CONTENT_ID) ?: "",
                type      = back.arguments?.getString(Screen.Player.ARG_TYPE) ?: "",
                title     = back.arguments?.getString(Screen.Player.ARG_TITLE) ?: "Stream",
                extension = back.arguments?.getString(Screen.Player.ARG_EXTENSION) ?: "mp4",
                categoryId = back.arguments?.getString(Screen.Player.ARG_CATEGORY_ID),
                onBack    = { navController.popBackStack() }
            )
        }
    }
    } // ProvideSharedClock
}
