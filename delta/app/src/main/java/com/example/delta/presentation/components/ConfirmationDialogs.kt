package com.example.delta.presentation.components

import android.util.Log
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
                        onDialogResponse: (Boolean) -> Unit,
                        onDismissDialogRequest: () -> Unit,
                        dialogText: String) {
    Dialog(
        showDialog = showConfirmationDialog,
        onDismissRequest = onDismissDialogRequest,
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
                    onClick = {
                        onDialogResponse(false)
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
                    onClick = { onDialogResponse(true) },
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