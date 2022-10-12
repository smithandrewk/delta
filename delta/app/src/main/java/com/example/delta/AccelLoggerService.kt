package com.example.delta

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class AccelLoggerService: Service(), SensorEventListener {
    private lateinit var activityChangeReceiver: ActivityChangeReceiver
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private val samplingRateHertz = 100

    private val numWindowsBatched = 1
    private val windowUpperLim = numWindowsBatched + 99
    private val windowRange: IntRange = numWindowsBatched..windowUpperLim

    private var xBuffer: MutableList<MutableList<Double>> = mutableListOf()
    private var yBuffer: MutableList<MutableList<Double>> = mutableListOf()
    private var zBuffer: MutableList<MutableList<Double>> = mutableListOf()
    private var extrasBuffer: MutableList<MutableList<String>> = mutableListOf()
    private lateinit var dataFolderName: String
    private lateinit var fRaw: FileOutputStream
    private lateinit var rawFilename: String
    private lateinit var fSession: FileOutputStream
    private lateinit var sessionFilename: String
    private lateinit var nHandler: NeuralHandler
    private var rawFileIndex: Int = 0
    private var sampleIndex: Int = 0
    private var currentActivity: String = "None"
    private val startTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

    private var activitiesDetected: MutableSet<String> = mutableSetOf()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i("0003", "Starting Accelerometer Service")

        // Setup Service Components
        createFiles()
        createBroadcastReceiver()
        createAccelerometerListener()
        nHandler = getNeuralHandler()
        // Start Service as foreground
        startForeground(1, createNotification())

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        /*
            We observed experimentally Ticwatch E samples at 100 Hz consistently for 9 hours
            in our app. Therefore, we take every 5th value from onsensorchanged to approximate
            20 Hz sampling rate.
         */
        if (sampleIndex == 5){
            sampleIndex = 0
            xBuffer.add(mutableListOf(event.values[0].toDouble()))
            yBuffer.add(mutableListOf(event.values[0].toDouble()))
            zBuffer.add(mutableListOf(event.values[0].toDouble()))
            extrasBuffer.add(mutableListOf(
                event.timestamp.toString(),
                Calendar.getInstance().timeInMillis.toString(),
                currentActivity
            ))
            if(xBuffer.size > windowUpperLim){
                activitiesDetected = nHandler.processBatch(extrasBuffer, xBuffer, yBuffer, zBuffer, fRaw)

                // clear buffer
                xBuffer = xBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                yBuffer = yBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                zBuffer = zBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                extrasBuffer = extrasBuffer.slice(windowRange)  as MutableList<MutableList<String>>

                if(activitiesDetected.size > 0){
                    Log.i("0003", "Broadcast: ${activitiesDetected.elementAt(0)}")
                    sendBroadcast(Intent(getString(R.string.ACTIVITY_DETECTED_BROADCAST_CODE))
                        .putStringArrayListExtra(getString(R.string.ACTIVITY),
                                                 ArrayList(activitiesDetected)))
                }
            }
            Log.i("0003","x: ${xBuffer.size}     y: ${yBuffer.size}    z: ${zBuffer.size}, extras: ${extrasBuffer.size}")
            Log.v("0003", "Time: ${event.timestamp}    x: ${event.values[0]}     y: ${event.values[1]}    z: ${event.values[2]}, activity: $currentActivity")
        }
        sampleIndex++
    }

    private fun createBroadcastReceiver() {
        // Create and register instance of broadcast receiver to receive signals from MainActivity
        activityChangeReceiver = ActivityChangeReceiver()
        registerReceiver(activityChangeReceiver,
                         IntentFilter(getString(R.string.ACTIVITY_CHANGE_BROADCAST_CODE)))
    }

    private fun createAccelerometerListener() {
        // Register Listener for Accelerometer Data
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)
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
            numWindowsBatched)
    }

    private fun createNotification(): Notification {
        // Create the NotificationChannel
        val channelName = "foreground_service_channel"
        val channelDescription = "Notifications for foreground service"
        val mChannel = NotificationChannel(
            getString(R.string.NOTIFICATION_CHANNEL_1_ID),
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        mChannel.description = channelDescription
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        // Create Notification
        return Notification.Builder(this, getString(R.string.NOTIFICATION_CHANNEL_1_ID))
            .setContentTitle("Delta")
            .setContentText("Accelerometer Recording")
            .setSmallIcon(R.drawable.ic_three_dots)
            .setContentIntent(null)     // Don't open any activity when Notification is clicked
            .setAutoCancel(false)       // Don't close notification when it's clicked
            .build()
    }

    private fun createFiles(){
        currentActivity = getString(R.string.NO_ACTIVITY)

        // Create folder for this session's files
        dataFolderName = startTimeReadable
        File(this.filesDir, dataFolderName).mkdir()
        createNewRawFile()

        // Create session file and first raw file
        sessionFilename = "Session.$startTimeReadable.csv"    // file to save session information
        fSession = FileOutputStream(File(this.filesDir, "$dataFolderName/$sessionFilename"))
        writeToSessionFile("File Start Time: ${Calendar.getInstance().timeInMillis}\n")
        writeToSessionFile("Event,Start Time,Stop Time\n")

        val fInfo = FileOutputStream(File(this.filesDir, "$dataFolderName/Info.txt"))
        fInfo.use { f ->
            f.write("Number of Windows in each Batch: $numWindowsBatched".toByteArray())
        }
    }

    private fun createNewRawFile() {
        // Create a new raw file for accelerometer data
        Log.i("0003", "Creating New Raw File")
        if (rawFileIndex != 0) {
            fRaw.close()
        }
        rawFilename = "$startTimeReadable.$rawFileIndex.csv"       // file to save raw data
        fRaw = FileOutputStream(File(this.filesDir, "$dataFolderName/$rawFilename"))
        fRaw.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity,label\n".toByteArray())
        rawFileIndex++
    }

    private fun writeToSessionFile(str: String) {
        // write a string to the session file
        Log.i("0003", "Writing to Session File")
        fSession = FileOutputStream(File(this.filesDir, "$dataFolderName/$sessionFilename"), true)
        fSession.use { f ->
            f.write(str.toByteArray())
        }
    }

    private inner class ActivityChangeReceiver : BroadcastReceiver() {
        // Inner class to define the broadcast receiver
        // This Broadcast Receiver receives signals from MainActivity when user presses buttons
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == getString(R.string.ACTIVITY_CHANGE_BROADCAST_CODE)) {
                currentActivity = intent.getStringExtra(getString(R.string.ACTIVITY)).toString()
                if(currentActivity == getString(R.string.NO_ACTIVITY)){
                    Log.i("0003", "Ended Activity")
                    writeToSessionFile("${Calendar.getInstance().timeInMillis}\n")
                    createNewRawFile()
                }
                else{
                    Log.i("0003", "Started $currentActivity")
                    writeToSessionFile("$currentActivity,${Calendar.getInstance().timeInMillis},")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister Accelerometer Listener and Broadcast Receiver
        Log.i("0003", "DESTROYED")
        sensorManager.unregisterListener(this)
        unregisterReceiver(activityChangeReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // do nothing
        return null
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }
}