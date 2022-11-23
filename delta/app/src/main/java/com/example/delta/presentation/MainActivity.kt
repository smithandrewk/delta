package com.example.delta.presentation

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.delta.R
import com.example.delta.presentation.navigation.Screen
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
                isSmoking = mViewModel.isSmoking,
                numberOfPuffs = mViewModel.totalNumberOfPuffsDetected,
                numberOfCigs = mViewModel.totalNumberOfCigsDetected,
                showConfirmSmokingDialog = mViewModel.showConfirmSmokingDialog,
                setShowConfirmSmokingDialog = { mViewModel.showConfirmSmokingDialog = it },
                onConfirmSmokingDialogResponse =  { mViewModel.onConfirmSmokingDialogResponse(it) },
                showConfirmDoneSmokingDialog = mViewModel.showConfirmDoneSmokingDialog,
                setShowConfirmDoneSmokingDialog = {mViewModel.showConfirmDoneSmokingDialog = it},
                onConfirmDoneSmokingDialogResponse = { mViewModel.onConfirmDoneSmokingDialogResponse(it) },
                showConfirmReportMissedCigDialog = mViewModel.showConfirmReportMissedCigDialog,
                setShowConfirmReportMissedCigDialog = { mViewModel.showConfirmReportMissedCigDialog = it },
                onConfirmReportMissedCigDialogResponse = {
                    mViewModel.onConfirmReportMissedCigDialogResponse(it)
                    if(it) {
                        navController.navigate(Screen.Time24hPicker.route)
                    }
                                                         },
                onClickIteratePuffsChip = { mViewModel.onPuffDetected() },
                onClickSmokingToggleChip = { mViewModel.onClickSmokingToggleChip(it) },
                onClickReportMissedCigChip = { mViewModel.onClickReportMissedCigChip() },
                onClickActivityPickerChip = {
                    mViewModel.onClickActivityPickerChip(it)
                    navController.popBackStack()
                }
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregister()
        filesHandler.closeRawFile()
    }
}