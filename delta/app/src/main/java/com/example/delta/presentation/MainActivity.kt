package com.example.delta.presentation

import  android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.delta.presentation.navigation.Screen
import com.example.delta.presentation.ui.MainViewModel
import com.example.delta.util.FilesHandler
import com.example.delta.util.SensorHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private val appStartTimeMillis = Calendar.getInstance().timeInMillis

    private lateinit var sensorHandler: SensorHandler
    private lateinit var filesHandler: FilesHandler

    private lateinit var mViewModel: MainViewModel
    private lateinit var filesDir: File
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTheme(android.R.style.Theme_DeviceDefault)

        // Initialize Objects
        mViewModel = MainViewModel(
            ::vibrateWatch,
            applicationContext,
            ::writeToLogFile,
            ::writeToEventsFile,
            ::writeStopSessionToEventsFile,
            ::writeFalseNegativeToEventsFile,
            ::navigateToSlider)
        filesDir = getExternalFilesDir(null)!!
        filesHandler = FilesHandler(applicationContext,filesDir, appStartTimeMillis, appStartTimeReadable)
        sensorHandler = SensorHandler(
            applicationContext,
            filesHandler,
            mViewModel,
            getSystemService(SENSOR_SERVICE) as SensorManager,
        )

        // UI
        setContent {
            navController = rememberSwipeDismissableNavController()
            navController.addOnDestinationChangedListener { controller, destination, arguments ->
                mViewModel.onDestinationChangedCallback(destination)
            }
            WearApp(
                swipeDismissibleNavController = navController,
                isSmoking = mViewModel.isSmoking,
                alertStatus = mViewModel.alertStatus,
                numberOfPuffs = mViewModel.totalNumberOfPuffsDetected,
                numberOfCigs = mViewModel.totalNumberOfCigsDetected,
                dialogText = mViewModel.mDialogText,
                showConfirmationDialog = mViewModel.showDialog,
                onDialogResponse = { mViewModel.onDialogResponse(it) },
                onClickIteratePuffsChip = { mViewModel.onPuffDetected() },
                onClickSmokingToggleChip = { mViewModel.onClickSmokingToggleChip() },
                onClickReportMissedCigChip = {
                    mViewModel.onClickReportMissedCigChip(
                        navigateToTimePicker = {
                            Log.d("0001","Navigating to time picker")
                            navController.navigate(Screen.Time24hPicker.route)
                        })
                                             },
                secondarySmokingText = mViewModel.secondarySmokingText,
                onTimePickerConfirm = {
                    mViewModel.onTimePickerConfirm(it)
                    navController.navigate(Screen.Slider.route)
                },
                onClickSliderScreenButton = {
                    mViewModel.onClickSliderScreenButton(it)
                    navController.navigate(Screen.WatchList.route)
                },
                onClickCigSliderScreenButton = {
                    mViewModel.onClickCigSliderScreenButton(it)
                    navController.popBackStack()
                },
                onClickActivityPickerChip = {
                    mViewModel.onClickActivityButton(it)
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.popBackStack()
                },
                onSubmitNewActivity = {mViewModel.onSubmitNewActivity(it)},
                activities = mViewModel.activities

                )

        }
    }
    private fun navigateToSlider(){
        navController.navigate(Screen.CigSlider.route)
    }
    private fun vibrateWatch() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, 255))
        } else {
            vibrator.vibrate(1000)
        }
    }
    private fun writeToLogFile(logEntry: String){
        filesHandler.writeToLogFile(logEntry)
    }
    private fun writeToEventsFile(event_id: Int) {
        filesHandler.writeToEventsFile(event_id)
    }
    private fun writeStopSessionToEventsFile(event_id: Int, satisfaction: Int){
        filesHandler.writeStopSessionToEventsFile(event_id, satisfaction)
    }
    private fun writeFalseNegativeToEventsFile(event_id: Int, dateTime: String, satisfaction: Int, otherActivity: String){
        filesHandler.writeNegativesToEventsFile(event_id, dateTime, satisfaction, otherActivity)
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregister()
        filesHandler.closeRawFile()
    }
}