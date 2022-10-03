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
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AccelLoggerService: Service(), SensorEventListener {
    private lateinit var activityChangeReceiver: ActivityChangeReceiver
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private val samplingRateHertz = 100

    private lateinit var dataFolderName: String
    private lateinit var fRaw: FileOutputStream
    private lateinit var rawFilename: String
    private lateinit var fSession: FileOutputStream
    private lateinit var sessionFilename: String
    private var rawFileIndex: Int = 0
    private var currentActivity: String = "None"
    private val startTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i("0003", "Starting Accelerometer Service")

        // Setup Service Components
        createFiles()
        createBroadcastReceiver()
        createAccelerometerListener()

        // Start Service as foreground
        startForeground(1, createNotification())

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Log data on every event received from accelerometer
        Log.v("0003", "Time: ${event.timestamp}    x: ${event.values[0]}     y: ${event.values[1]}    z: ${event.values[2]}")
        fRaw.write((event.timestamp.toString()+","+
                    event.values[0].toString()+","+
                    event.values[1].toString()+","+
                    event.values[2].toString()+","+
                    Calendar.getInstance().timeInMillis+","+
                    currentActivity+"\n").toByteArray())
    }

    private fun createBroadcastReceiver() {
        // Create and register instance of broadcast receiver to receive signals from MainActivity
        activityChangeReceiver = ActivityChangeReceiver()
        registerReceiver(activityChangeReceiver, IntentFilter(getString(R.string.BROADCAST_CODE)))
    }

    private fun createAccelerometerListener() {
        // Register Listener for Accelerometer Data
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)
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
//        val pendingIntent: PendingIntent =
//            Intent(this, MainActivity::class.java).let { notificationIntent ->
//                PendingIntent.getActivity(
//                    this, 0, notificationIntent,
//                    PendingIntent.FLAG_IMMUTABLE
//                )
//            }
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
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity\n".toByteArray())
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
            if (intent.action == getString(R.string.BROADCAST_CODE)) {
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