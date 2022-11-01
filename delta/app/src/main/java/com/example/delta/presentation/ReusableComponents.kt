package com.example.delta.presentation

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.delta.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*

@Composable
fun SelfReportChip (
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        onClick = { /* Log smoking to session */ },
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