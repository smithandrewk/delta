package com.example.delta.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.MutableLiveData
import com.example.delta.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), SensorEventListener {
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private lateinit var dataFolderName: String
    private lateinit var fRaw: FileOutputStream
    private lateinit var fFalseNegatives: FileOutputStream

    private lateinit var rawFilename: String
    private lateinit var fSession: FileOutputStream
    private lateinit var sessionFilename: String
    private val numWindowsBatched = 1
    private var rawFileIndex: Int = 0
    private var currentActivity: String = "None"
    private lateinit var nHandler: NeuralHandler
    private lateinit var falseNegativesFilename: String
    private lateinit var falseNegativesFile: File
    private var userDeclaredTheyAreSmoking: Boolean = false
    private val liveData = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(this,liveData)
        }
        createInitialFiles()
        createAndRegisterAccelerometerListener()
        nHandler = getNeuralHandler()

    }
    private fun createAndRegisterAccelerometerListener() {
        // Register Listener for Accelerometer Data
        val samplingRateHertz = 100
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)
    }
    fun onSmokeToggle(clicked: Boolean) {
        if (clicked) {
            sendSessionDetectedConfirmationDialog()
            // Session start
            Log.i("0001", "Start")
        }
        else {
            Log.i("0001", "Stop")
        }
    }
    override fun onSensorChanged(event: SensorEvent) {
        /*
            We observed experimentally Ticwatch E samples at 100 Hz consistently for 9 hours
            in our app. Therefore, we take every 5th value from onsensorchanged to approximate
            20 Hz sampling rate.
         */
        writeToRawFile("${event.timestamp},${event.values[0]},${event.values[1]},${event.values[2]}")
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }
    private fun createInitialFiles(){
        currentActivity = getString(R.string.NO_ACTIVITY)

        // Create folder for this session's files
        dataFolderName = appStartTimeReadable
        File(this.filesDir, dataFolderName).mkdir()
        createNewRawFile()
        createFalseNegativesFile()
        // Create session file and first raw file
        sessionFilename = "Session.$appStartTimeReadable.csv"    // file to save session information
        fSession = FileOutputStream(File(this.filesDir, "$dataFolderName/$sessionFilename"))
        writeToSessionFile("File Start Time: ${Calendar.getInstance().timeInMillis}\n")
        writeToSessionFile("Event,Start Time,Stop Time\n")

        val fInfo = FileOutputStream(File(this.filesDir, "$dataFolderName/Info.txt"))
        fInfo.use { f ->
            f.write("Number of Windows in each Batch: $numWindowsBatched".toByteArray())
        }
    }
    private fun writeToSessionFile(str: String) {
        // write a string to the session file
        Log.i("0003", "Writing to Session File")
        fSession = FileOutputStream(File(this.filesDir, "$dataFolderName/$sessionFilename"), true)
        fSession.use { f ->
            f.write(str.toByteArray())
        }
    }
    private fun writeToRawFile(str: String){
        fRaw.write("${str}\n".toByteArray())
    }
    private fun createNewRawFile() {
        // Create a new raw file for accelerometer data
        Log.i("0003", "Creating New Raw File")
        if (rawFileIndex != 0) {
            fRaw.close()
        }
        rawFilename = "$appStartTimeReadable.$rawFileIndex.csv"       // file to save raw data
        fRaw = FileOutputStream(File(this.filesDir, "$dataFolderName/$rawFilename"))
        fRaw.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity,label,state\n".toByteArray())
        rawFileIndex++
    }
    private fun getNeuralHandler(): NeuralHandler{
        // Load ANN weights and input ranges
        // TODO: Can we move loading the weights to the NeuralHandler class?
        var ins: InputStream = resources.openRawResource(R.raw.input_to_hidden_weights_and_biases)
        val inputToHiddenWeightsAndBiasesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        ins = resources.openRawResource(R.raw.hidden_to_output_weights_and_biases)
        val hiddenToOutputWeightsAndBiasesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        ins = resources.openRawResource(R.raw.input_ranges)
        val inputRangesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        return NeuralHandler(
            "andrew",
            inputToHiddenWeightsAndBiasesString,
            hiddenToOutputWeightsAndBiasesString,
            inputRangesString,
            numWindowsBatched,applicationContext)
    }
    fun onReportFalseNegative(){
        falseNegativesFile.appendText("${Calendar.getInstance().timeInMillis}\n")
    }
    private fun createFalseNegativesFile(){
        falseNegativesFilename = "False-Negatives.$dataFolderName.csv"
        falseNegativesFile = File(this.filesDir, "$dataFolderName/$falseNegativesFilename")
        fFalseNegatives = FileOutputStream(falseNegativesFile)
        fFalseNegatives.use { f ->
            f.write("Time\n".toByteArray())
        }
    }
    fun onSessionDetectedByModel(){
        // TODO
        if(userDeclaredTheyAreSmoking){
            /*
                if user declared they are smoking,
                then don't send dialog
             */
        } else {
            sendSessionDetectedConfirmationDialog()
        }
    }
    private fun sendSessionDetectedConfirmationDialog(){
        liveData.postValue(true)
    }
    fun setLiveDataFalse(){
        liveData.postValue(false)
    }
}