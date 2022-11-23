package com.example.delta.presentation

import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.delta.R
import com.example.delta.presentation.navigation.Screen
import com.example.delta.presentation.ui.MainViewModel
import com.example.delta.util.FilesHandler
import com.example.delta.util.SensorHandler
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private val appStartTimeMillis = Calendar.getInstance().timeInMillis

    private lateinit var sensorHandler: SensorHandler
    private lateinit var filesHandler: FilesHandler

    // UI
    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setTheme(android.R.style.Theme_DeviceDefault)
        mViewModel = MainViewModel(::vibrateWatch,applicationContext,::writeFalseNegativeToFile)
        filesHandler = FilesHandler(this.filesDir, mViewModel, appStartTimeMillis, appStartTimeReadable)
        sensorHandler = SensorHandler(
            applicationContext,
            filesHandler,
            mViewModel,
            getSystemService(SENSOR_SERVICE) as SensorManager,
        )

        setContent {
            navController = rememberSwipeDismissableNavController()
            navController.addOnDestinationChangedListener { controller, destination, arguments ->
                mViewModel.onDestinationChangedCallback(destination)
            }
            WearApp(
                swipeDismissibleNavController = navController,
                isSmoking = mViewModel.isSmoking,
                numberOfPuffs = mViewModel.totalNumberOfPuffsDetected,
                numberOfCigs = mViewModel.totalNumberOfCigsDetected,
                dialogText = mViewModel.mDialogText,
                showConfirmationDialog = mViewModel.showDialog,
                onDialogResponse = { mViewModel.onDialogResponse(it) },
                onDismissDialogRequest = { mViewModel.dismissDialog() },
                onClickIteratePuffsChip = { mViewModel.onPuffDetected() },
                onClickSmokingToggleChip = { mViewModel.onClickSmokingToggleChip() },
                onClickReportMissedCigChip = {
                    mViewModel.onClickReportMissedCigChip(
                        navigateToTimePicker = {
                            if(it) navController.navigate(Screen.Time24hPicker.route)
                        })
                                             },
                secondarySmokingText = mViewModel.secondarySmokingText,
                onTimePickerConfirm = {
                    navController.popBackStack()
                    mViewModel.onTimePickerConfirm(it)
                    navController.navigate(Screen.Slider.route)
                },
                onClickSliderScreenButton = {
                    navController.popBackStack()
                    mViewModel.onClickSliderScreenButton(it)
                    navController.navigate(Screen.WatchList.route)
                },
                onClickActivityPickerChip = {
                    navController.popBackStack()
                    mViewModel.onClickActivityButton(it)
                                            },
                onSubmitNewActivity = {mViewModel.onSubmitNewActivity(it)},
                activities = mViewModel.activities

                )
        }
    }
    private fun vibrateWatch() {
        Log.d("0000", "vibrating watch")
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, 255))
        } else {
            vibrator.vibrate(1000)
        }
    }
    fun writeFalseNegativeToFile(dateTimeForUserInput: LocalDateTime, satisfaction: Int, otherActivity: String){
        filesHandler.writeFalseNegativeToFile(dateTimeForUserInput,satisfaction,otherActivity)
    }
    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregister()
        filesHandler.closeRawFile()
    }
}