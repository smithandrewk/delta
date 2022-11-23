package com.example.delta.presentation


import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.fadeAway
import com.example.delta.presentation.navigation.DestinationScrollType
import com.example.delta.presentation.navigation.SCROLL_TYPE_NAV_ARGUMENT
import com.example.delta.presentation.theme.WearAppTheme
import com.example.delta.presentation.theme.initialThemeValues
import java.time.LocalDateTime
import com.google.android.horologist.compose.layout.fadeAwayScalingLazyList
import com.example.delta.presentation.ui.ScalingLazyListStateViewModel
import com.example.delta.presentation.ui.ScrollStateViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.delta.presentation.components.ActivityPickerScreen
import com.example.delta.presentation.components.CustomTimeText
import com.example.delta.presentation.components.SliderScreen
import com.google.android.horologist.composables.TimePicker
import com.example.delta.presentation.navigation.Screen
import com.example.delta.presentation.ui.landing.LandingScreen
import com.example.delta.util.FilesHandler
import com.example.delta.util.SensorHandler

@Composable
fun WearApp(
    modifier: Modifier = Modifier,
    swipeDismissibleNavController: NavHostController = rememberSwipeDismissableNavController(),
    filesHandler: FilesHandler,
    isSmoking: Boolean,
    numberOfPuffs: Int,
    numberOfCigs: Int,
    showConfirmSmokingDialog: Boolean,
    setShowConfirmSmokingDialog: (Boolean) -> Unit,
    onConfirmSmokingDialogResponse: (Boolean) -> Unit,
    showConfirmDoneSmokingDialog: Boolean,
    setShowConfirmDoneSmokingDialog: (Boolean) -> Unit,
    onConfirmDoneSmokingDialogResponse: (Boolean) -> Unit,
    showConfirmReportMissedCigDialog: Boolean,
    setShowConfirmReportMissedCigDialog: (Boolean) -> Unit,
    onConfirmReportMissedCigDialogResponse: (Boolean) -> Unit,
    onClickIteratePuffsChip: () -> Unit,
    onClickSmokingToggleChip: (Boolean) -> Unit,
    onClickReportMissedCigChip : () -> Unit,
    onClickActivityPickerChip: (String) -> Unit
) {
    var themeColors by remember { mutableStateOf(initialThemeValues.colors) }
    WearAppTheme(colors = themeColors) {
        // Allows user to show/hide the vignette on appropriate screens.
        // IMPORTANT NOTE: Usually you want to show the vignette all the time on screens with
        // scrolling content, a rolling side button, or a rotating bezel. This preference is just
        // to visually demonstrate the vignette for the developer to see it on and off.
        var vignetteVisiblePreference by rememberSaveable { mutableStateOf(true) }

        // Observes the current back stack entry to pull information and determine if the screen
        // is scrollable and the scrollable state.
        //
        // The main reason the state for any scrollable screen is hoisted to this level is so the
        // Scaffold can properly place the position indicator (also known as the scroll indicator).
        //
        // We save the above scrollable states in the SavedStateHandle and retrieve them
        // when needed from custom view models (see ScrollingViewModels class).
        //
        // Screens with scrollable content:
        //  1. The watch list screen uses ScalingLazyColumn (backed by ScalingLazyListState)
        //  2. The watch detail screens uses Column with scrolling enabled (backed by ScrollState).
        //
        // We also use these scrolling states for various other things (like hiding the time
        // when the user is scrolling and only showing the vignette when the screen is
        // scrollable).
        //
        // Remember, mobile guidelines specify that if you back navigate out of a screen and then
        // later navigate into it again, it should be in its initial scroll state (not the last
        // scroll location it was in before you backed out).
        val currentBackStackEntry by swipeDismissibleNavController.currentBackStackEntryAsState()

        val scrollType =
            currentBackStackEntry?.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)
                ?: DestinationScrollType.NONE
        var dateTimeForUserInput by remember { mutableStateOf(LocalDateTime.now()) }
        var displayValueForUserInput by remember { mutableStateOf(5) }

        Scaffold(
            modifier = modifier,
            timeText = {
                // Scaffold places time at top of screen to follow Material Design guidelines.
                // (Time is hidden while scrolling.)

                val timeTextModifier =
                    when (scrollType) {
                        DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING -> {
                            val scrollViewModel: ScalingLazyListStateViewModel =
                                viewModel(currentBackStackEntry!!)
                            Modifier.fadeAwayScalingLazyList {
                                scrollViewModel.scrollState
                            }
                        }
                        DestinationScrollType.COLUMN_SCROLLING -> {
                            val viewModel: ScrollStateViewModel =
                                viewModel(currentBackStackEntry!!)
                            Modifier.fadeAway {
                                viewModel.scrollState
                            }
                        }
                        DestinationScrollType.TIME_TEXT_ONLY -> {
                            Modifier
                        }
                        else -> {
                            null
                        }
                    }
                key(currentBackStackEntry?.destination?.route) {
                    CustomTimeText(
                        modifier = timeTextModifier ?: Modifier,
                        visible = timeTextModifier != null,
                        startText = null
                    )
                }
            },
            vignette = {
                // Only show vignette for screens with scrollable content.
                if (scrollType == DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING ||
                    scrollType == DestinationScrollType.COLUMN_SCROLLING
                ) {
                    if (vignetteVisiblePreference) {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                }
            },
            positionIndicator = {
                // Only displays the position indicator for scrollable content.
                when (scrollType) {
                    DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING -> {
                        // Get or create the ViewModel associated with the current back stack entry
                        val scrollViewModel: ScalingLazyListStateViewModel =
                            viewModel(currentBackStackEntry!!)
                        PositionIndicator(scalingLazyListState = scrollViewModel.scrollState)
                    }
                    DestinationScrollType.COLUMN_SCROLLING -> {
                        // Get or create the ViewModel associated with the current back stack entry
                        val viewModel: ScrollStateViewModel = viewModel(currentBackStackEntry!!)
                        PositionIndicator(scrollState = viewModel.scrollState)
                    }
                }
            }
        ) {
            SwipeDismissableNavHost(
                navController = swipeDismissibleNavController,
                startDestination = Screen.Landing.route,
                modifier = Modifier.background(MaterialTheme.colors.background)
            ) {
                // Main Window
                composable(
                    route = Screen.Landing.route,
                    arguments = listOf(
                        // In this case, the argument isn't part of the route, it's just attached
                        // as information for the destination.
                        navArgument(SCROLL_TYPE_NAV_ARGUMENT) {
                            type = NavType.EnumType(DestinationScrollType::class.java)
                            defaultValue = DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
                        }
                    )
                ) {
                    val scalingLazyListState = scalingLazyListState(it)

                    val focusRequester = remember { FocusRequester() }
                    LandingScreen(
                        scalingLazyListState = scalingLazyListState,
                        focusRequester = focusRequester,
                        isSmoking = isSmoking,
                        numberOfPuffs = numberOfPuffs,
                        numberOfCigs = numberOfCigs,
                        showConfirmSmokingDialog = showConfirmSmokingDialog,
                        setShowConfirmSmokingDialog = setShowConfirmSmokingDialog,
                        onConfirmSmokingDialogResponse = onConfirmSmokingDialogResponse,
                        showConfirmDoneSmokingDialog = showConfirmDoneSmokingDialog,
                        setShowConfirmDoneSmokingDialog = setShowConfirmDoneSmokingDialog,
                        onConfirmDoneSmokingDialogResponse = onConfirmDoneSmokingDialogResponse,
                        showConfirmReportMissedCigDialog = showConfirmReportMissedCigDialog,
                        setShowConfirmReportMissedCigDialog = setShowConfirmReportMissedCigDialog,
                        onConfirmReportMissedCigDialogResponse = onConfirmReportMissedCigDialogResponse,
                        onClickIteratePuffsChip = onClickIteratePuffsChip,
                        onClickSmokingToggleChip = onClickSmokingToggleChip,
                        onClickReportMissedCigChip = onClickReportMissedCigChip
                    )

                    RequestFocusOnResume(focusRequester)
                }
                composable(Screen.Time24hPicker.route) {
                    TimePicker(
                        onTimeConfirm = {
                            swipeDismissibleNavController.popBackStack()
                            dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
                            swipeDismissibleNavController.navigate(Screen.Slider.route)

//                            filesHandler.writeFalseNegativeToFile(dateTimeForUserInput)
//                            iterateNumberOfCigs()
                        },
                        time = dateTimeForUserInput.toLocalTime(),
                        showSeconds = false
                    )
                }
                composable(route = Screen.Slider.route) {
                    SliderScreen(
                        displayValue = displayValueForUserInput,
                        onValueChange = {
                            displayValueForUserInput = it
                        },
                        onClickSliderScreenButton = {
                            Log.d("0000","herere")
                            swipeDismissibleNavController.popBackStack()
                            swipeDismissibleNavController.navigate(Screen.WatchList.route)

                        }
                    )

                }
                composable(
                    route = Screen.WatchList.route,
                    arguments = listOf(
                        // In this case, the argument isn't part of the route, it's just attached
                        // as information for the destination.
                        navArgument(SCROLL_TYPE_NAV_ARGUMENT) {
                            type = NavType.EnumType(DestinationScrollType::class.java)
                            defaultValue = DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
                        }
                    )
                ) {
                    val scalingLazyListState = scalingLazyListState(it)
                    val focusRequester = remember { FocusRequester() }


                    ActivityPickerScreen(
                        watches = listOf("smoking","vaping","eating"),
                        scalingLazyListState = scalingLazyListState,
                        focusRequester = focusRequester,
                        onClickWatch = onClickActivityPickerChip
                    )

                    RequestFocusOnResume(focusRequester)
                }
            }
        }
        // end wear app theme
    }
}
@Composable
private fun scalingLazyListState(it: NavBackStackEntry): ScalingLazyListState {
    val passedScrollType = it.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)

    check(
        passedScrollType == DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
    ) {
        "Scroll type must be DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING"
    }

    val scrollViewModel: ScalingLazyListStateViewModel = viewModel(it)

    return scrollViewModel.scrollState
}

@Composable
private fun RequestFocusOnResume(focusRequester: FocusRequester) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            focusRequester.requestFocus()
        }
    }
}