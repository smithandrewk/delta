package com.example.delta.presentation

import  android.hardware.SensorManager
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.TimeText
import com.example.delta.util.BatteryHandler
import com.example.delta.util.FileHandler
import com.example.delta.util.SensorHandler
import java.util.*
import androidx.wear.compose.material.curvedText

class MainActivity : ComponentActivity() {
    private lateinit var mSensorHandler: SensorHandler
    private lateinit var mFileHandler: FileHandler
    private lateinit var mBatteryHandler: BatteryHandler
    private val mMainViewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTheme(android.R.style.Theme_DeviceDefault)

        mFileHandler = FileHandler(getExternalFilesDir(null)!!)
        mSensorHandler = SensorHandler(mFileHandler,getSystemService(SENSOR_SERVICE) as SensorManager)
        mBatteryHandler = BatteryHandler(::registerReceiver,::unregisterReceiver, mFileHandler, mMainViewModel::updateBatteryLevel)

        setContent {
            WearApp(mMainViewModel)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mSensorHandler.unregisterAll()
        mFileHandler.closeFiles()
        mBatteryHandler.unregister()
    }
}
@Composable
fun WearApp(viewModel: MainViewModel) {
    val currentBatteryLevel = viewModel.currentBatteryLevel
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Black,
        topBar = {
            TimeText(
                endCurvedContent = {
                    curvedText(
                        text = "${currentBatteryLevel.toInt()}%",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "$currentBatteryLevel %"
        )
    }
}

class MainViewModel(): ViewModel() {
    var currentBatteryLevel by mutableStateOf(0f)
    fun updateBatteryLevel(newLevel: Float) {
        currentBatteryLevel = newLevel
    }
}