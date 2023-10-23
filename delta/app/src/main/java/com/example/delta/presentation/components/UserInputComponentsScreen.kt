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
package com.example.delta.presentation.components

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.SpannableString
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.example.delta.R
import com.google.android.horologist.compose.navscaffold.scrollableColumn
import java.time.LocalDateTime

/**
 * Shows different input options like Pickers, Steppers and Sliders
 */
@Composable
fun UserInputComponentsScreen(
    scalingLazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    value: Int,
    dateTime: LocalDateTime,
    onClickStepper: () -> Unit,
    onClickSlider: () -> Unit,
    onClickDemoDatePicker: () -> Unit,
    onClickDemo12hTimePicker: () -> Unit,
    onClickDemo24hTimePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textForUserInput by remember { mutableStateOf("") }
    var textForVoiceInput by remember { mutableStateOf("") }

    val inputTextKey = "input_text"

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val newInputText: CharSequence? = results.getCharSequence(inputTextKey)
                textForUserInput = newInputText.toString()
            }
        }

    val voiceLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.let { data ->
                val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                textForVoiceInput = results?.get(0) ?: "None"
            }
        }

    ScalingLazyColumn(
        modifier = modifier.scrollableColumn(focusRequester, scalingLazyListState),
        state = scalingLazyListState,
        autoCentering = AutoCenteringParams(itemIndex = 0)
    ) {
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
