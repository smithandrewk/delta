package com.example.delta.presentation

import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.delta.R
import com.example.delta.presentation.ui.MainViewModel
import com.example.delta.util.FilesHandler
import com.example.delta.util.NeuralHandler
import com.example.delta.util.SensorHandler
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    internal lateinit var navController: NavHostController
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private val appStartTimeMillis = Calendar.getInstance().timeInMillis

    private lateinit var sensorHandler: SensorHandler
    private lateinit var filesHandler: FilesHandler

    // UI
    private val mViewModel: MainViewModel = MainViewModel()
//    lateinit var timer: CountDownTimer
//    val sessionLengthMillis: Long = 10000
//    private val progressIndicatorIterator: Float = 0.1f
//    private var currentTimerProgress: Long = 0
//    private val countDownIntervalMillis: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        filesHandler = FilesHandler(this.filesDir, mViewModel, appStartTimeMillis, appStartTimeReadable)
        sensorHandler = SensorHandler(
            applicationContext,
            filesHandler,
            mViewModel,
            getSystemService(SENSOR_SERVICE) as SensorManager,
        )

        setContent {
            navController = rememberSwipeDismissableNavController()

            WearApp(
                swipeDismissibleNavController = navController,
                filesHandler = filesHandler,
                sensorHandler = sensorHandler,
                isSmokingState = mViewModel.isSmokingState,
                numberOfPuffs = mViewModel.numberOfPuffsState,
                numberOfCigs = mViewModel.numberOfCigsState,
                onClickSmokingToggleChip = {
                    Log.d("0000","here")
                    if(!it) mViewModel.numberOfCigsState ++
                    mViewModel.isSmokingState = !mViewModel.isSmokingState
                                           },
                iterateNumberOfCigs = {mViewModel.numberOfCigsState ++},
                onClickIteratePuffsChip = { mViewModel.onPuffDetected() },
                alertShowDialog = mViewModel.alertShowDialog,
                setAlertShowDialog = { mViewModel.alertShowDialog = it },
                showConfirmDoneSmokingDialog = mViewModel.showConfirmDoneSmokingDialog,
                setIsSmoking = {
                    Log.d("0000","here $it")
                    mViewModel.isSmokingState = it },
                setShowConfirmDoneSmokingDialog = {mViewModel.showConfirmDoneSmokingDialog = it}
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregister()
        filesHandler.closeRawFile()
    }
}