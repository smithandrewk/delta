package com.example.delta.presentation

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.example.delta.presentation.theme.DeltaTheme

@Composable
fun WearApp(uiState: MainUiState,viewModel: MainViewModel,instance: MainActivity){
    DeltaTheme {
        val listState = rememberScalingLazyListState()
        val animatedProgress by animateFloatAsState(
            targetValue = uiState.progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        )
        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState
            ) {
                item { WelcomeText() }
                item { SmokeStatisticsCard(
                    numberOfCigs = uiState.numberOfCigs,
                    numberOfPuffs = uiState.numberOfPuffs)}
                item { ReportMissedCigChip(
                    chipText = "Report missed cig",
                    onChipClick = {
                        viewModel.setShowConfirmReportFalseNegativeDialog(it)
                    })
                }
                item {
                    if(uiState.isSmoking){
                        CompactCallbackChip(
                            chipText="Finish cig",
                            chipColor = "#827978",
                            onChipClick = {
                                viewModel.setShowConfirmDoneSmokingDialog(true)
                            })
                    } else {
                        CompactCallbackChip(
                            chipText="Smoke cig",
                            chipColor = "#b52914",
                            onChipClick = {
                                viewModel.setShowConfirmSmokingDialog(true)
                            })
                    }
                }
            }
            ReportMissedCigDialog(
                showDialog = uiState.showConfirmReportFalseNegativeDialog,
                onDialogResponse = {
                    viewModel.setShowConfirmReportFalseNegativeDialog(false)
                    if(it) {
                        viewModel.iterateNumberOfCigs()
                        instance.onReportFalseNegative()
                    }
                })
            ConfirmSmokeACigDialog(
                showDialog = uiState.showConfirmSmokingDialog,
                onDialogResponse = {
                    viewModel.setShowConfirmSmokingDialog(false)
                    Log.d("0000",uiState.isSmoking.toString())
                    if(it) instance.startSmoking(instance.sessionLengthMillis,0.0f)
                })
            ConfirmDoneSmokingDialog(
                showDialog = uiState.showConfirmDoneSmokingDialog,
                onDialogResponse = {
                    viewModel.setShowConfirmDoneSmokingDialog(false)
                    if(it) instance.stopSmoking()
                })
            if(uiState.isSmoking){
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize(),
                    startAngle = 290.0f,
                    endAngle = 250.0f,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
@Composable
fun CompactCallbackChip(chipText:String,chipColor:String,onChipClick: (Boolean) -> Unit){
    CompactChip(
        onClick = { onChipClick(true) },
        enabled = true,
        // CompactChip label should be no more than 1 line of text
        label = {
            Text(chipText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        icon = {
            Icon(
                painter = painterResource(id = com.example.delta.R.drawable.lungs),
                contentDescription = "airplane",
                modifier = Modifier.size(ChipDefaults.SmallIconSize),
            )
        },
        colors = ChipDefaults.chipColors(backgroundColor = Color(android.graphics.Color.parseColor(chipColor)))
    )
}
@Composable
fun ConfirmDoneSmokingDialog(showDialog: Boolean,onDialogResponse: (Boolean) -> Unit){
    // TODO add timer to dialog
    val scrollState = rememberScalingLazyListState()
    Dialog(
        showDialog = showDialog,
        onDismissRequest = { },     // do not allow dismissal without responding
        scrollState = scrollState
    ) {
        Alert(
            scrollState = scrollState,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding =
            PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
            icon = {
                Icon(
                    painter = painterResource(id = com.example.delta.R.drawable.lungs),
                    contentDescription = "lungs",
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center),
                )
            },
            title = { Text(text = "Are you done smoking?", textAlign = TextAlign.Center) },
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Chip(
                        label = { Text("Yes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        onClick = { onDialogResponse(true) },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier.width(60.dp)
                    )

                    Chip(
                        label = { Text("No", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        onClick = { onDialogResponse(false) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.width(60.dp)
                    )

                }
            }
        }
    }
}
@Composable
fun ConfirmSmokeACigDialog(showDialog: Boolean,onDialogResponse: (Boolean) -> Unit){
    // TODO add timer to dialog
    val scrollState = rememberScalingLazyListState()
    Dialog(
        showDialog = showDialog,
        onDismissRequest = { },     // do not allow dismissal without responding
        scrollState = scrollState
    ) {
        Alert(
            scrollState = scrollState,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding =
            PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
            icon = {
                Icon(
                    painter = painterResource(id = com.example.delta.R.drawable.lungs),
                    contentDescription = "lungs",
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center),
                )
            },
            title = { Text(text = "Do you want to record a smoking session?", textAlign = TextAlign.Center) },
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Chip(
                        label = { Text("Yes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        onClick = { onDialogResponse(true) },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier.width(60.dp)
                    )

                    Chip(
                        label = { Text("No", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        onClick = { onDialogResponse(false) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.width(60.dp)
                    )

                }
            }
        }
    }
}
@Composable
fun SmokeStatisticsCard(numberOfCigs: Int, numberOfPuffs: Int){
    Card(
        onClick = { /* ... */ }
    ) {
        Text("Looks like you've only smoked $numberOfCigs cigs and $numberOfPuffs puffs!")
    }
}
@Composable
fun ReportMissedCigChip(chipText:String,onChipClick: (Boolean) -> Unit){
    CompactChip(
        onClick = { onChipClick(true) },
        enabled = true,
        // CompactChip label should be no more than 1 line of text
        label = {
            Text(chipText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        icon = {
            Icon(
                painter = painterResource(id = com.example.delta.R.drawable.lungs),
                contentDescription = "airplane",
                modifier = Modifier.size(ChipDefaults.SmallIconSize),
            )
        },
    )
}
@Composable
fun ReportMissedCigDialog(showDialog: Boolean,onDialogResponse: (Boolean) -> Unit){
    // TODO add timer to dialog
    val scrollState = rememberScalingLazyListState()
    Dialog(
        showDialog = showDialog,
        onDismissRequest = { onDialogResponse(false) },     // do not allow dismissal without responding
        scrollState = scrollState
    ) {
        Alert(
            scrollState = scrollState,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding =
            PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
            icon = {
                Icon(
                    painter = painterResource(id = com.example.delta.R.drawable.lungs),
                    contentDescription = "lungs",
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center),
                )
            },
            title = { Text(text = "Do you want to report a missed cig?", textAlign = TextAlign.Center) },
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Chip(
                        label = { Text("Yes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        onClick = { onDialogResponse(true) },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier.width(60.dp)
                    )

                    Chip(
                        label = { Text("No", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        onClick = { onDialogResponse(false) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.width(60.dp)
                    )

                }
            }
        }
    }
}
@Composable
fun WelcomeText(modifier: Modifier = Modifier){
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(id = com.example.delta.R.string.title_text)
    )
}