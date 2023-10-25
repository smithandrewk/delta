package com.example.delta.presentation

import  android.hardware.SensorManager
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.delta.util.FileHandler
import com.example.delta.util.SensorHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

    private lateinit var sensorHandler: SensorHandler
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTheme(android.R.style.Theme_DeviceDefault)

        // Initialize Objects
        fileHandler = FileHandler(getExternalFilesDir(null)!!)
        sensorHandler = SensorHandler(
            fileHandler,
            getSystemService(SENSOR_SERVICE) as SensorManager
        )

        setContent {
            WearApp(appStartTimeReadable)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregisterAll()
        fileHandler.closeFiles()
    }
}