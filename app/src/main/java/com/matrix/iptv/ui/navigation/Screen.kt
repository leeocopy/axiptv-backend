package com.matrix.iptv.ui.navigation

// ── skill_2.md §9 Navigation routes ─────────────────────────────────────────
sealed class Screen(val route: String) {

    // ─ Auth/Gate ───────────────────────────────────────────────────────────
    object Splash              : Screen("splash")

    /** Trial gate — shows remaining days, Try App / Activate Now */
    object ActivationOverlay   : Screen("activation_overlay")

    /** Purchase/activate page — QR code + device ID + link */
    object ActivatePurchase    : Screen("activate_purchase")

    // ─ Profile ─────────────────────────────────────────────────────────────
    object ProfilePicker       : Screen("profile_picker")
    object ProfileAdd          : Screen("profile_add")
    object ProfileEdit         : Screen("profile_edit/{profileId}") {
        fun createRoute(profileId: String) = "profile_edit/$profileId"
        const val ARG = "profileId"
    }

    // ─ Home ────────────────────────────────────────────────────────────────
    object HomeShell           : Screen("home_shell")
    object HomeDashboard       : Screen("home_dashboard")
    object LiveCategories      : Screen("live_categories")
    object VodCategories       : Screen("vod_categories")
    object SeriesCategories    : Screen("series_categories")
    object Favorites           : Screen("favorites")
    object Music               : Screen("music")
    object Entertainment       : Screen("entertainment")
    object Radio               : Screen("radio")
    object Devotional          : Screen("devotional")
    object Search              : Screen("search")
    object Settings            : Screen("settings")

    // ─ Content Deeper ─────────────────────────────────────────────────────
    object LiveStreams : Screen("live_streams/{categoryId}") {
        fun createRoute(categoryId: String) = "live_streams/$categoryId"
        const val ARG_CATEGORY_ID = "categoryId"
    }

    object VodStreams : Screen("vod_streams/{categoryId}") {
        fun createRoute(categoryId: String) = "vod_streams/$categoryId"
        const val ARG_CATEGORY_ID = "categoryId"
    }

    object VodDetails : Screen("vod_details/{streamId}") {
        fun createRoute(streamId: Int) = "vod_details/$streamId"
        const val ARG_STREAM_ID = "streamId"
    }

    object SeriesStreams : Screen("series_streams/{categoryId}") {
        fun createRoute(categoryId: String) = "series_streams/$categoryId"
        const val ARG_CATEGORY_ID = "categoryId"
    }

    object SeriesDetails : Screen("series_details/{seriesId}") {
        fun createRoute(seriesId: Int) = "series_details/$seriesId"
        const val ARG_SERIES_ID = "seriesId"
    }

    // ─ Player ──────────────────────────────────────────────────────────────
    object Player : Screen("player/{contentId}/{type}/{title}/{extension}") {
        fun createRoute(contentId: String, type: String, title: String = "Stream", extension: String = "mp4") = 
            "player/$contentId/$type/$title/$extension"
        const val ARG_CONTENT_ID = "contentId"
        const val ARG_TYPE       = "type"
        const val ARG_TITLE      = "title"
        const val ARG_EXTENSION  = "extension"
    }

    // Legacy alias kept so existing code compiles; routes to ActivationOverlay
    object TrialActivation : Screen("activation_overlay")
}
