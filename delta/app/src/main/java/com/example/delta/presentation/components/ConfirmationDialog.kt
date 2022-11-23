package com.example.delta.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.example.delta.R

@Composable
fun ConfirmSmokingDialog(scrollState: ScalingLazyListState,
                         showConfirmSmokingDialog: Boolean,
                         onConfirmSmokingDialogResponse: (Boolean) -> Unit) {
    Dialog(
        showDialog = showConfirmSmokingDialog,
        onDismissRequest = { onConfirmSmokingDialogResponse(false) },
        scrollState = scrollState
    ) {
        Alert(
            title = {
                Text(
                    text = "Are you smoking",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            },
            negativeButton = {
                Button(
                    onClick = {
                        onConfirmSmokingDialogResponse(false)
                    },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "no"
                    )
                }
            },
            positiveButton = {
                Button(
                    onClick = { onConfirmSmokingDialogResponse(true) },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "yes"
                    )
                }
            },
            scrollState = scrollState
        ) {
            Text(
                text = stringResource(R.string.dialog_sure),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}
@Composable
fun ConfirmDoneSmokingDialog(scrollState: ScalingLazyListState,
                             showConfirmDoneSmokingDialog: Boolean,
                             onConfirmDoneSmokingDialogResponse: (Boolean) -> Unit){

    Dialog(
        showDialog = showConfirmDoneSmokingDialog,
        onDismissRequest = { onConfirmDoneSmokingDialogResponse(false) },
        scrollState = scrollState
    ) {
        Alert(
            title = {
                Text(
                    text = "Are you done smoking",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            },
            negativeButton = {
                Button(
                    onClick = { onConfirmDoneSmokingDialogResponse(false) },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "no"
                    )
                }
            },
            positiveButton = {
                Button(
                    onClick = { onConfirmDoneSmokingDialogResponse(true) },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "yes"
                    )
                }
            },
            scrollState = scrollState
        ) {
            Text(
                text = stringResource(R.string.dialog_sure),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}
@Composable
fun ConfirmReportMissedCigDialog(scrollState: ScalingLazyListState,
                             showConfirmReportMissedCigDialog: Boolean,
                             onConfirmReportMissedCigDialogResponse: (Boolean) -> Unit){
    Dialog(
        showDialog = showConfirmReportMissedCigDialog,
        onDismissRequest = { onConfirmReportMissedCigDialogResponse(false) },
        scrollState = scrollState
    ) {
        Alert(
            title = {
                Text(
                    text = "Confirm that you want to report a cig that you smoked.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            },
            negativeButton = {
                Button(
                    onClick = { onConfirmReportMissedCigDialogResponse(false) },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "no"
                    )
                }
            },
            positiveButton = {
                Button(
                    onClick = { onConfirmReportMissedCigDialogResponse(true) },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "yes"
                    )
                }
            },
            scrollState = scrollState
        ) {
            Text(
                text = "Confirm?",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}
