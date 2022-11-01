/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.delta.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.delta.R
import com.example.delta.presentation.theme.DeltaTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var accelIntent: Intent
    private lateinit var dataFolderName: String
    private lateinit var fPuffs: FileOutputStream
    private lateinit var puffsFilename: String

    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(this)
        }
//        createFile()

        // Start Accelerometer Service
        // start service to record accelerometer data
//        accelIntent = Intent(applicationContext, AccelLoggerService::class.java)
//            .putExtra("StartTime", appStartTimeReadable)
//        startForegroundService(accelIntent)
    }
    private fun createFile(){
        dataFolderName = appStartTimeReadable
        puffsFilename = "Smoking-Sessions.$dataFolderName.csv"
        fPuffs = FileOutputStream(File(this.filesDir, "$dataFolderName/$puffsFilename"))
        fPuffs.use { f ->
            f.write("Start Time,Stop Time\n".toByteArray())
        }
    }

    fun onSmokeToggle(clicked: Boolean) {
        if (clicked) {
            // Session start
            Log.i("0001", "Start")
        }
        else {
            Log.i("0001", "Stop")
        }
    }
}

@Composable
fun WearApp(instance: MainActivity) {
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
            item { SelfReportChip(contentModifier, iconModifier) }
            item { SessionStartToggleButton(contentModifier, instance) }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(MainActivity())
}