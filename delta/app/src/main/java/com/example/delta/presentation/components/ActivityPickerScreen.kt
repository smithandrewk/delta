package com.example.delta.presentation.components

/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.*

/**
 * Displays a Slider, which allows users to make a selection from a range of values.
 */

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.style.TextOverflow

import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.items
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.example.delta.R

// Material 3 components
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.google.android.horologist.compose.navscaffold.scrollableColumn
import dagger.hilt.android.qualifiers.ApplicationContext


/**
 * Displays a list of watches plus a [ToggleChip] at the top to display/hide the Vignette around
 * the screen. The list is powered using a [ScalingLazyColumn].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityPickerScreen(
    applicationContext: Context,
    activities: List<String>,
    scalingLazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    onClickActivity: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClickCreateNewActivityButton: () -> Unit,
    onSubmitNewFNActivity: (String) -> Unit
) {
    var textForUserInput by remember { mutableStateOf("") }

    val inputTextKey = "input_text"

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val newInputText: CharSequence? = results.getCharSequence(inputTextKey)
                textForUserInput = newInputText.toString()
                onSubmitNewFNActivity(textForUserInput)
            }
        }
    ScalingLazyColumn(
        modifier = modifier.scrollableColumn(focusRequester, scalingLazyListState),
        state = scalingLazyListState
    ) {
        // Displays all watches.
        items(activities) { string ->
            Chip(
                onClick = { /* Nothing. This can't be clicked */ },
                label = {
                    Text(
                        text = string,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )

            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(80),
                modifier = Modifier.combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        Toast.makeText(applicationContext, "Press and Hold to Select", Toast.LENGTH_SHORT).show();
                    },
                    onLongClick = {
                        Log.d("ActivityPickerScreen", "Long Press")
                        onClickActivity(string)
                    }
                ).fillMaxWidth().height(52.dp)
            ) {}
        }

        item {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs: List<RemoteInput> = listOf(
                RemoteInput.Builder(inputTextKey)
                    .setLabel(stringResource(R.string.manual_text_entry_label))
                    .wearableExtender {
                        setEmojisAllowed(true)
                        setInputActionType(EditorInfo.IME_ACTION_DONE)
                    }.build()
            )

            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)

            Chip(
                onClick = {
                    launcher.launch(intent)
                    onClickCreateNewActivityButton()
                },
                label = {
                    Text(
                        stringResource(R.string.text_input_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    Text(
                        text = textForUserInput
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
