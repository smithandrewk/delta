package com.example.delta

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.delta.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
//    private lateinit var sensorManager: SensorManager
//    private val samplingRateHertz = 200
//    private val samplingPeriodSeconds = 1/samplingRateHertz
//    private val samplingPeriodMicroseconds = samplingPeriodSeconds * 1000000
//    private var mAccel: Sensor? = null

    private lateinit var accelIntent: Intent

    private lateinit var dataFolderName: String
    private lateinit var fRaw: FileOutputStream
    private lateinit var rawFilename: String
    private lateinit var fSession: FileOutputStream
    private lateinit var sessionFilename: String
    private var rawFileIndex: Int = 0
    private var currentActivity: String = "None"

    private var calendar = Calendar.getInstance()
    private var startTimeMillis = calendar.timeInMillis
    private val startTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

    private val LAUNCH_END_BUTTON_CODE = 1
    private lateinit var binding: ActivityMainBinding

    private val activityOptions = mapOf(R.id.eatButton to "Eating",
                                        R.id.drinkButton to "Drinking",
                                        R.id.smokeButton to "Smoking",
                                        R.id.otherButton to "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("0001", "CREATED")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // start service
        accelIntent = Intent(applicationContext, AccelLoggerService::class.java)
        startForegroundService(accelIntent)

        // create folder for this session's files
        dataFolderName = startTimeReadable
        File(this.filesDir, dataFolderName).mkdir()

        // start Recording on app creation
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        calendar = Calendar.getInstance()
        startTimeMillis = calendar.timeInMillis

        createNewRawFile()

        // create Session file
        sessionFilename = "Session.$startTimeReadable.csv"    // file to save session information
        fSession = FileOutputStream(File(this.filesDir, "$dataFolderName/$sessionFilename"))
        writeToSessionFile("File Start Time: $startTimeMillis\n")
        writeToSessionFile("Event,Start Time,Stop Time\n")

//        mAccel?.also { accel ->
//            sensorManager.registerListener(this, accel,
//                samplingPeriodMicroseconds, samplingPeriodMicroseconds)
//        }

        // get chosen activity from user - create onClickListener for each button
        activityOptions.forEach { (button, chosenActivity) ->
            findViewById<Button>(button).setOnClickListener {
                Log.i("0001", "Started $chosenActivity")
                currentActivity = chosenActivity

                // tell service that new activity is starting
                sendBroadcast(Intent(getString(R.string.BROADCAST_CODE)).putExtra("ACTIVITY", chosenActivity))

                // log start time to session file
                calendar = Calendar.getInstance()
                val time = calendar.timeInMillis
                writeToSessionFile("$chosenActivity,$time,")

                // start end button activity
                val endButtonIntent = Intent(this, EndActivityButton::class.java)
//                endButtonIntent.putExtra("FilenameKey", rawFilename)
//                endButtonIntent.putExtra("SamplingRateKey", "$samplingRateHertz")
                startActivityForResult(endButtonIntent, LAUNCH_END_BUTTON_CODE)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Receives result from the ChooseActivity activity, and calls beginActivity with that result
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_END_BUTTON_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("0001", "Logging end activity")
                calendar = Calendar.getInstance()
                val time = calendar.timeInMillis
                writeToSessionFile("$time\n")
                currentActivity = getString(R.string.NO_ACTIVITY)
                createNewRawFile()

                val broadcastIntent = Intent(
                    getString(R.string.BROADCAST_CODE)).putExtra(getString(R.string.ACTIVITY),
                    getString(R.string.NO_ACTIVITY))
                sendBroadcast(broadcastIntent)

            }
            else {
                Log.i("0001", "error")
            }
        }
    }

    private fun createNewRawFile() {
        if (rawFileIndex != 0) {
            fRaw.close()
        }
        rawFilename = "$startTimeReadable.$rawFileIndex.csv"       // file to save raw data
        fRaw = FileOutputStream(File(this.filesDir, "$dataFolderName/$rawFilename"))
        if (rawFileIndex == 0) {
            fRaw.write("File Start Time: $startTimeMillis\n".toByteArray())
        }
        else {
            calendar = Calendar.getInstance()
            val time = calendar.timeInMillis
            fRaw.write("File Start Time: $time\n".toByteArray())
        }
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity\n".toByteArray())
        rawFileIndex++
    }

    private fun writeToSessionFile(str: String) {
        fSession = FileOutputStream(File(this.filesDir, "$dataFolderName/$sessionFilename"), true)
        fSession.use { f ->
            f.write(str.toByteArray())
        }
    }

//    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
//
//    override fun onSensorChanged(event: SensorEvent) {
//        calendar = Calendar.getInstance()
//        val time = calendar.timeInMillis
//        fRaw.write((event.timestamp.toString()+","+
//                    event.values[0].toString()+","+
//                    event.values[1].toString()+","+
//                    event.values[2].toString()+","+
//                    time+","+
//                    currentActivity+"\n").toByteArray())
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("0001", "Saved instance")
//        outState.putString("RawFilename", rawFilename)
//        outState.putInt("RawFileIndex", rawFileIndex)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i("0001", "Restored instance")
//        rawFilename = savedInstanceState.getString("RawFilename") ?: rawFilename
//        rawFileIndex = savedInstanceState.getInt("RawFileIndex")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.i("0001", "DESTROYED")
        stopService(accelIntent)
    }
    override fun onStop() {
        super.onStop()
        Log.i("0001", "STOPPED")
    }
    override fun onPause() {
        super.onPause()
        Log.i("0001", "PAUSED")
    }
    override fun onStart() {
        super.onStart()
        Log.i("0001", "STARTED")
    }
    override fun onRestart() {
        super.onRestart()
        Log.i("0001", "RESTARTED")
    }
    override fun onResume() {
        super.onResume()
        Log.i("0001", "RESUMED")
    }
}