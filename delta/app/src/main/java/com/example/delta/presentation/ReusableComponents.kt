package com.example.delta.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import com.example.delta.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.example.delta.presentation.theme.DeltaTheme

@Composable
fun WearApp(instance: MainActivity,liveData: LiveData<Boolean>) {
    DeltaTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        val listState = rememberScalingLazyListState()

        val contentModifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        val iconModifier = Modifier
            .size(24.dp)
            .wrapContentSize(align = Alignment.Center)

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 64.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.Center,
            state = listState
        ) {
            item { SelfReportChip(contentModifier, iconModifier, instance) }
            item { SessionStartToggleButton(contentModifier, instance) }
            item { ShowDialogExample(liveData,instance)}
        }
    }
}
@Composable
fun SelfReportChip (
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    instance: MainActivity
) {
    Chip(
        modifier = modifier,
        onClick = { instance.onReportFalseNegative() },
        label = {
            Text(
                text = "I just smoked dummy",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.lungs),
                contentDescription = "triggers meditation action",
                modifier = iconModifier
            )
        },
    )
}


@Composable
fun SessionStartToggleButton(
    modifier: Modifier = Modifier,
//    iconModifier: Modifier = Modifier
    instance: MainActivity
) {
    var clicked by remember { mutableStateOf(true) }
    ToggleButton(
        checked = clicked,
        onCheckedChange = {
            Log.i("Components", "unchecked")
            instance.onSmokeToggle(clicked)
            clicked = !clicked
            // todo update text
                          },
        modifier = modifier,
        enabled = true,
    ) {
        Text(
            text = "I am about to smoke",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
//        Icon(
//            painter = painterResource(id = R.drawable.smoking),
//            contentDescription = "smoking icon",
//            modifier = iconModifier,
//        )
    }
}



@Composable
fun ShowDialogExample(show: LiveData<Boolean>,instance: MainActivity){
    val scrollState = rememberScalingLazyListState()
    val showDialog: Boolean? by show.observeAsState()

    showDialog?.let {
        Dialog(
            showDialog = it,
            onDismissRequest = { instance.setLiveDataFalse() },
            scrollState = scrollState
        ) {
            Alert(
                scrollState = scrollState,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                contentPadding =
                PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lungs),
                        contentDescription = "airplane",
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                title = { Text(text = "Example Title Text", textAlign = TextAlign.Center) },
                message = {
                    Text(
                        text = "Message content goes here",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                },
            ) {
                item {
                    Chip(
                        label = { Text("Primary") },
                        onClick = { instance.setLiveDataFalse()  },
                        colors = ChipDefaults.primaryChipColors(),
                    )
                }
                item {
                    Chip(
                        label = { Text("Secondary") },
                        onClick = { instance.setLiveDataFalse()  },
                        colors = ChipDefaults.secondaryChipColors(),
                    )
                }
            }
        }
    }
}