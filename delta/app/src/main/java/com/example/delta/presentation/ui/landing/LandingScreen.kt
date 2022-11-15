package com.example.delta.presentation.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.foundation.radialGradientBackground
import androidx.wear.compose.material.*
import com.google.android.horologist.compose.navscaffold.scrollableColumn
import com.example.delta.presentation.ui.util.ReportFullyDrawn
import com.example.delta.R

/**
 * Simple landing page with three actions, view a list of watches, toggle on/off text before the
 * time or view a demo of different user input components.
 *
 * A text label indicates the screen shape and places it at the bottom of the screen.
 * If it's a round device, it will curve the text along the bottom curve. Otherwise, for a square
 * device, it's a regular Text composable.
 */
@Composable
fun LandingScreen(
    scalingLazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    onClickWatchList: () -> Unit,
    proceedingTimeTextEnabled: Boolean,
    onClickProceedingTimeText: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Places both Chips (button and toggle) in the middle of the screen.
        ScalingLazyColumn(
            modifier = Modifier.scrollableColumn(focusRequester, scalingLazyListState),
            state = scalingLazyListState,
            autoCentering = AutoCenteringParams(itemIndex = 0)
        ) {
            item {
                // Signify we have drawn the content of the first screen
                ReportFullyDrawn()
                ReportMissedCigChip (onClickWatchList)
            }
        }

        // Places curved text at the bottom of round devices and straight text at the bottom of
        // non-round devices.
        if (LocalConfiguration.current.isScreenRound) {
            val watchShape = stringResource(R.string.watch_shape)
            val primaryColor = MaterialTheme.colors.primary
            CurvedLayout(
                anchor = 90F,
                anchorType = AnchorType.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                curvedRow {
                    curvedText(
                        text = watchShape,
                        angularDirection = CurvedDirection.Angular.CounterClockwise,
                        style = CurvedTextStyle(
                            fontSize = 18.sp,
                            color = primaryColor
                        ),
                        modifier = CurvedModifier
                            .radialGradientBackground(
                                0f to Color.Transparent,
                                0.2f to Color.DarkGray.copy(alpha = 0.2f),
                                0.6f to Color.DarkGray.copy(alpha = 0.2f),
                                0.7f to Color.DarkGray.copy(alpha = 0.05f),
                                1f to Color.Transparent
                            )
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.3f to Color.DarkGray.copy(alpha = 0.05f),
                                0.4f to Color.DarkGray.copy(alpha = 0.2f),
                                0.8f to Color.DarkGray.copy(alpha = 0.2f),
                                1f to Color.Transparent
                            )
                        ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = stringResource(R.string.watch_shape),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun ReportMissedCigChip(onClickWatchList: () -> Unit) {
    Chip(
        onClick = onClickWatchList,
        label = {
            Text(
                stringResource(R.string.report_false_negative_button_label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}
@Composable
fun SmokingToggleChip(proceedingTimeTextEnabled: Boolean,onClickProceedingTimeText: (Boolean) -> Unit){
    ToggleChip(
        modifier = Modifier.fillMaxWidth(),
        checked = proceedingTimeTextEnabled,
        onCheckedChange = onClickProceedingTimeText,
        label = {
            Text(
                text = stringResource(R.string.start_smoking_session_button_label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(
                    checked = proceedingTimeTextEnabled
                ),
                contentDescription = if (proceedingTimeTextEnabled) "On" else "Off"
            )
        }
    )
}