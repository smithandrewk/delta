package com.example.delta.presentation

import  android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
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
        filesDir = getExternalFilesDir(null)!!
        mViewModel = MainViewModel(
            ::vibrateWatch,
            applicationContext,
            filesDir,
            ::writeToLogFile,
            ::writeToEventsFile,
            ::writeStopSessionToEventsFile,
            ::writeFalseNegativeToEventsFile,
            ::writeFalsePositiveToEventsFile,
            ::navigateToFnSlider,
            ::navigateToFpActivitiesList
        )
        filesHandler = FilesHandler(applicationContext,filesDir, appStartTimeMillis, appStartTimeReadable)
        sensorHandler = SensorHandler(
            applicationContext,
            filesHandler,
            mViewModel,
            getSystemService(SENSOR_SERVICE) as SensorManager
        )

        // UI
        setContent {
            navController = rememberSwipeDismissableNavController()
            navController.addOnDestinationChangedListener { controller, destination, arguments ->
                mViewModel.onDestinationChangedCallback(destination)
            }
            WearApp(
                applicationContext,
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

                // Click Report FN
                onClickReportMissedCigChip = {
                    mViewModel.onClickReportMissedCigChip(
                        navigateToTimePicker = {
                            Log.d("0001","Navigating to time picker")
                            navController.navigate(Screen.Time24hPicker.route)
                        }
                    )
                },
                secondarySmokingText = mViewModel.secondarySmokingText,
                // Select time that false negative occurred
                onFnTimePickerConfirm = {
                    mViewModel.onFnTimePickerConfirm(it)
                    navController.navigate(Screen.Slider.route)
                },
                // Select Enjoyment after false negative reported smoke session
                onClickFnSliderScreenButton = {
                    mViewModel.onClickFnSliderScreenButton(it)
                    navController.navigate(Screen.FnActivityList.route)
                },
                // Select Enjoyment after detected/manually started smoking session
                onClickCigSliderScreenButton = {
                    mViewModel.onClickCigSliderScreenButton(it)
                    navController.popBackStack()
                },
                // While reporting false negative, choose activity user was doing
                onClickFNActivityPickerChip = {
                    mViewModel.onClickFNActivityButton(it)
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.popBackStack()
                },
                // Submit new false negative activity
                onSubmitNewFNActivity = {mViewModel.onSubmitNewFNActivity(it)},
                fnActivities = mViewModel.fnActivities,
                // Submit new false positive activity
                onSubmitNewFpActivity = {mViewModel.onSubmitNewFpActivity(it)},
                fpActivities = mViewModel.fpActivities,
                // While reporting false positive, choose activity user was doing
                onClickFpActivityPickerChip = {
                    mViewModel.onClickFPActivityButton(it)
                    navController.popBackStack()
                },
                heroText = appStartTimeReadable
                )
        }
    }
    private fun navigateToFnSlider(){
        navController.navigate(Screen.CigSlider.route)
    }
    private fun navigateToFpActivitiesList() {
        navController.navigate(Screen.FpActivityList.route)
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
    private fun writeFalsePositiveToEventsFile(event_id: Int, otherActivity: String) {
        filesHandler.writePositivesToEventsFile(event_id, otherActivity)
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregister()
        filesHandler.closeRawFile()
    }
}