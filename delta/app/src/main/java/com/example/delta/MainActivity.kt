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

//    private lateinit var sessionData: MutableList<String>
    private var currentTime = System.currentTimeMillis()

    private lateinit var xmlFilename: CurvedTextView
    private lateinit var xmlSamplingFrequency: CurvedTextView

    private lateinit var binding: ActivityMainBinding

    private val activityOptions = mapOf(R.id.eatButton to "Eating",
                                        R.id.drinkButton to "Drinking",
                                        R.id.smokeButton to "Smoking",
                                        R.id.otherButton to "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // start Recording on app creation
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        currentTime = System.currentTimeMillis()
        createNewRawFile()

        // create Session file
        sessionFilename = "Session.$currentTime.csv"    // file to save session information
        fSession = this.openFileOutput(sessionFilename, Context.MODE_PRIVATE)
        fSession.write("Event, Start Time, Stop Time".toByteArray())

        mAccel?.also { accel ->
            sensorManager.registerListener(this, accel,
                samplingPeriodMicroseconds, samplingPeriodMicroseconds)
        }

        // get chosen activity from user - create onClickListener for each button and send corresponding string
        activityOptions.forEach { (button, chosenActivity) ->
            findViewById<Button>(button).setOnClickListener {
                Log.i("0001", "Started $chosenActivity")
                currentActivity = chosenActivity
                // TODO write to session file
                // TODO open "End activity" page
            }
        }
    }

    private fun createNewRawFile(){
        if (rawFileIndex != 0){
            fRaw.close()
        }
        rawFilename = "$currentTime.$rawFileIndex.csv"       // file to save raw data

//        xmlFilename.text = rawFilename          // set filename for xml

        fRaw = this.openFileOutput(rawFilename, Context.MODE_PRIVATE)
        fRaw.write("Recording Real Start Time: $currentTime\n".toByteArray())
        fRaw.write("timestamp,acc_x,acc_y,acc_z\n".toByteArray())
        rawFileIndex++
    }
    private fun beginActivity(){
        // logs a session to session csv

    }
    private fun endActivity(){
        // log end of session to session file
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        fRaw.write((event.timestamp.toString()+","+
                event.values[0].toString()+","+
                event.values[1].toString()+","+
                event.values[2].toString()+"\n").toByteArray())
    }
}