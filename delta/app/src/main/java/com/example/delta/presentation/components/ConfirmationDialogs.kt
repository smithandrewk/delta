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
fun ConfirmationDialog(scrollState: ScalingLazyListState,
                        showConfirmationDialog: Boolean,
                        onDialogResponse: (String) -> Unit,
                        dialogText: String) {
    Dialog(
        showDialog = showConfirmationDialog,
        onDismissRequest = { onDialogResponse("dismiss") },
        scrollState = scrollState
    ) {
        Alert(
            title = {
                Text(
                    text = dialogText,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            },
            negativeButton = {
                Button(
                    onClick = { onDialogResponse("no") },
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
                    onClick = {  onDialogResponse("yes") },
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