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
                        alertShowDialog: Boolean,
                        setAlertShowDialog: (Boolean) -> Unit,
                        setIsSmoking: (Boolean) -> Unit){
    var alertStatus by remember { mutableStateOf("") }
    val dialogDismissed = stringResource(R.string.dialog_dismissed)
    val dialogNo = stringResource(R.string.confirmation_dialog_no)
    val dialogYes = stringResource(R.string.alert_dialog_yes)
    Dialog(
        showDialog = alertShowDialog,
        onDismissRequest = {
            if (alertStatus.isEmpty()) alertStatus = dialogDismissed
            setAlertShowDialog(false)
        },
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
                        alertStatus = dialogNo
                        setIsSmoking(false)
                        setAlertShowDialog(false)
                    },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = dialogNo
                    )
                }
            },
            positiveButton = {
                Button(
                    onClick = {
                        alertStatus = dialogYes
                        setIsSmoking(true)
                        setAlertShowDialog(false)
                    },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = dialogYes
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
                             setShowConfirmDoneSmokingDialog: (Boolean) -> Unit,
                         setIsSmoking: (Boolean) -> Unit){
    var alertStatus by remember { mutableStateOf("") }
    val dialogDismissed = stringResource(R.string.dialog_dismissed)
    val dialogNo = stringResource(R.string.confirmation_dialog_no)
    val dialogYes = stringResource(R.string.alert_dialog_yes)
    Dialog(
        showDialog = showConfirmDoneSmokingDialog,
        onDismissRequest = {
            if (alertStatus.isEmpty()) alertStatus = dialogDismissed
            setIsSmoking(false)
            setShowConfirmDoneSmokingDialog(false)
        },
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
                    onClick = {
                        alertStatus = dialogNo
                        setIsSmoking(true)
                        setShowConfirmDoneSmokingDialog(false)
                    },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = dialogNo
                    )
                }
            },
            positiveButton = {
                Button(
                    onClick = {
                        alertStatus = dialogYes
                        setIsSmoking(false)
                        setShowConfirmDoneSmokingDialog(false)
                    },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = dialogYes
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
