package com.example.delta.presentation

import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.delta.R
import com.example.delta.presentation.ui.MainViewModel
import com.example.delta.util.FilesHandler
import com.example.delta.util.SensorHandler
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    internal lateinit var navController: NavHostController
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private val appStartTimeMillis = Calendar.getInstance().timeInMillis

    // Files
//    private lateinit var dataFolderName: String
//    private var rawFileIndex: Int = 0
//    private lateinit var fRaw: FileOutputStream         // File output stream to write raw acc data
//    private lateinit var falseNegativesFile: File       // File to write false negative events
//    private lateinit var eventsFile: File               // File to write smoking events
//    private lateinit var positivesFile: File            // File to write smoking detected events
//    // TODO positive puffs

    // Record raw data
//    private lateinit var sensorManager: SensorManager
//    private var sampleIndex: Int = 0
//    private val numWindowsBatched = 1
//    private var xBuffer:MutableList<MutableList<Double>> = mutableListOf()
//    private var yBuffer:MutableList<MutableList<Double>> = mutableListOf()
//    private var zBuffer:MutableList<MutableList<Double>> = mutableListOf()
//    private var extrasBuffer:MutableList<MutableList<String>> = mutableListOf()
//    private val windowUpperLim = numWindowsBatched + 99
//    private val windowRange:IntRange = numWindowsBatched..windowUpperLim
    private lateinit var sensorHandler: SensorHandler

    // Neural Network
//    private lateinit var nHandler: NeuralHandler
//    private var currentActivity: String = "None"
//    var isSmoking: Boolean = false

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

        val filesHandler = FilesHandler(this.filesDir, mViewModel, appStartTimeMillis, appStartTimeReadable)

        sensorHandler = SensorHandler(
            applicationContext,
            filesHandler,
            mViewModel,
            getSystemService(SENSOR_SERVICE) as SensorManager
        )

        setContent {
            navController = rememberSwipeDismissableNavController()

            WearApp(
                swipeDismissableNavController = navController,
                filesHandler = filesHandler,
                sensorHandler = sensorHandler
            )
        }

    }
//    private fun createInitialFiles(){
//        currentActivity = getString(R.string.NO_ACTIVITY)
//
//        // Create folder for this session's files
//        dataFolderName = appStartTimeReadable
//        File(this.filesDir, dataFolderName).mkdir()
//        createNewRawFile()
//
//        // Event Recording Files
//        // TODO add source of end
//        eventsFile = File(this.filesDir, "$dataFolderName/Self-Report.$dataFolderName.csv")
//        eventsFile.appendText("Event,Start Time,Stop Time\n")
//
//        falseNegativesFile = File(this.filesDir, "$dataFolderName/False-Negatives.$dataFolderName.csv")
//        falseNegativesFile.appendText("timeInMillis,userEstimatedTimeOfFalseNegative\n")
//
//        positivesFile = File(this.filesDir, "$dataFolderName/Positives.$dataFolderName.csv")
//        positivesFile.appendText("Time \n")
//
//        // Info File
//        try {
//            val json = JSONObject()
//                .put("App Start Time", appStartTimeMillis)
//                .put("App Start Time Readable", appStartTimeReadable)
//                .put("Number of Windows Batched", numWindowsBatched)
//            File(this.filesDir, "$dataFolderName/Info.json").appendText(json.toString())
//        } catch (e: Exception) { e.printStackTrace() }
//    }
//    // Setup Functions
//    private fun createNewRawFile() {
//        // Create a new raw file for accelerometer data
//        Log.i("0003", "Creating New Raw File")
//        if (rawFileIndex == 0) {
//            // Create "raw" directory
//            File(this.filesDir, "$dataFolderName/raw").mkdir()
//        }
//        else {
//            fRaw.close()
//        }
//        val rawFilename = "$appStartTimeReadable.$rawFileIndex.csv"
//        fRaw = FileOutputStream(File(this.filesDir, "$dataFolderName/raw/$rawFilename"))
//        fRaw.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
//        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity,label,state\n".toByteArray())
//        rawFileIndex++
//    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregister()
    }
}