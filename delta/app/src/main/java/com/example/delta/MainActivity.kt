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
import android.widget.ToggleButton
import androidx.wear.widget.CurvedTextView
import com.example.delta.databinding.ActivityMainBinding
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val samplingRateHertz = 1
    private val samplingPeriodSeconds = 1/samplingRateHertz
    private val samplingPeriodMicroseconds = samplingPeriodSeconds * 1000000
    private var mAccel: Sensor? = null

    private lateinit var fRaw: FileOutputStream
    private lateinit var rawFilename: String
    private lateinit var fSession: FileOutputStream
    private lateinit var sessionFilename: String
    private var rawFileIndex: Int = 0
    private var currentActivity: String = "None"

    private var currentTimeMillis = System.currentTimeMillis()
    private val readableTime = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

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

        // start Recording on app creation
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        currentTimeMillis = System.currentTimeMillis()
        createNewRawFile()

        // create Session file
        sessionFilename = "Session.$currentTimeMillis.csv"    // file to save session information
        fSession = this.openFileOutput(sessionFilename, Context.MODE_PRIVATE)
        fSession.write("Event, Start Time, Stop Time\n".toByteArray())

        mAccel?.also { accel ->
            sensorManager.registerListener(this, accel,
                samplingPeriodMicroseconds, samplingPeriodMicroseconds)
        }

        // get chosen activity from user - create onClickListener for each button and send corresponding string
        activityOptions.forEach { (button, chosenActivity) ->
            findViewById<Button>(button).setOnClickListener {
                Log.i("0001", "Started $chosenActivity")
                currentActivity = chosenActivity
                // log start time to session file
                val startTime = System.currentTimeMillis()
                fSession.write("$chosenActivity, $startTime, ".toByteArray())
                // start end button activity
                val endButtonIntent = Intent(this, EndActivityButton::class.java)
                endButtonIntent.putExtra("FilenameKey", rawFilename)
                endButtonIntent.putExtra("SamplingRateKey", "$samplingRateHertz")
                startActivity(endButtonIntent)
            }
        }
        // check if returning from EndActivityButton activity
        val activityEnding: String? = intent.getStringExtra("EndActivityKey")
        if (activityEnding != null){
            // log end of activity
            val stopTime = System.currentTimeMillis()
            fSession.write("$stopTime\n".toByteArray())
            currentActivity = "None"
        }
        else{
            Log.i("0001", "activityEnding is null")
        }
    }

    private fun createNewRawFile(){
        if (rawFileIndex != 0){
            fRaw.close()
        }
        rawFilename = "$currentTimeMillis.$rawFileIndex.csv"       // file to save raw data

        fRaw = this.openFileOutput(rawFilename, Context.MODE_PRIVATE)
        fRaw.write("Recording Real Start Time: $currentTimeMillis\n".toByteArray())
        fRaw.write("timestamp,acc_x,acc_y,acc_z\n".toByteArray())
        rawFileIndex++
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val time = System.currentTimeMillis()
        fRaw.write((event.timestamp.toString()+","+
                event.values[0].toString()+","+
                event.values[1].toString()+","+
                event.values[2].toString()+","+
                time+","+
                currentActivity+"\n").toByteArray())
    }

    override fun onDestroy() {
        super.onDestroy()
        fSession.close()
        fRaw.close()
        Log.i("0001", "DESTROYED")
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