package com.example.delta.presentation.navigation

// Used as a Navigation Argument for the WatchDetail Screen.
const val WATCH_ID_NAV_ARGUMENT = "watchId"

// Navigation Argument for Screens with scrollable types:
// 1. WatchList -> ScalingLazyColumn
// 2. WatchDetail -> Column (with scaling enabled)
const val SCROLL_TYPE_NAV_ARGUMENT = "scrollType"

/**
 * Represent all Screens (Composables) in the app.
 */
sealed class Screen(
    val route: String
) {
    object Landing : Screen("landing")
    object WatchList : Screen("watchList")
    object WatchDetail : Screen("watchDetail")
    object UserInputComponents : Screen("userInputComponents")
    object Stepper : Screen("stepper")
    object Slider : Screen("slider")
    object CigSlider : Screen("cigslider")
    object Map : Screen("map")
    object DatePicker : Screen("date")
    object Time12hPicker : Screen("time12h")
    object Time24hPicker : Screen("time24h")
    object Dialogs : Screen("dialogs")
    object ProgressIndicators : Screen("progressIndicators")
    object IndeterminateProgressIndicator : Screen("indeterminateProgressIndicator")
    object FullScreenProgressIndicator : Screen("fullScreenProgressIndicator")
    object Theme : Screen("theme")
}
