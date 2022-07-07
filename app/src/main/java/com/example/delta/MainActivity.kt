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
import android.view.View
import android.widget.ToggleButton
import androidx.wear.widget.CurvedTextView
import com.example.delta.databinding.ActivityMainBinding
import java.io.FileOutputStream


class MainActivity : Activity(), SensorEventListener {
    private val LAUNCH_CHOOSE_ACTIVITY = 1

    private lateinit var binding: ActivityMainBinding

    private lateinit var sensorManager: SensorManager
    private val samplingRateHertz = 1
    private val samplingPeriodSeconds = 1/samplingRateHertz
    private val samplingPeriodMicroseconds = samplingPeriodSeconds * 1000000
    private var mAccel: Sensor? = null

    private lateinit var fRaw: FileOutputStream
    private lateinit var rawFilename: String
    private lateinit var fSession: FileOutputStream
    private lateinit var sessionFilename: String
    
    private lateinit var sessionData: MutableList<String>
    private var currentTime = System.currentTimeMillis()
    
    private lateinit var beginToggle: ToggleButton
    private lateinit var recordToggle: ToggleButton

    private lateinit var xmlFilename: CurvedTextView
    private lateinit var xmlSamplingFrequency: CurvedTextView

    private var userid: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        xmlSamplingFrequency = findViewById(R.id.samplingFrequency)
        xmlFilename = findViewById(R.id.filename)

        xmlSamplingFrequency.text = "$samplingRateHertz Hz"

        recordToggle = findViewById(R.id.activityToggleButton)
        recordToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onRecordStart()
                Log.i("0001", "on")
            } else {
                onRecordStop()
                Log.i("0001", "off")
            }
        }

        beginToggle = findViewById(R.id.beginToggleButton)
        beginToggle.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked){
//                beginActivity()
                // launch new app activity to choose what activity to record
                var i = Intent(this, ChooseActivity::class.java)
                startActivityForResult(i, LAUNCH_CHOOSE_ACTIVITY)
            } else {
                endActivity()
            }
        }
    }

//    override fun onResume(){
//        super.onResume()
//        // check if activity is chosen
//        var extras: Bundle? = intent.extras
//        Log.i("0001", "resume")
//        if(extras != null){
//            val chosenActivity: String? = extras.getString("chosenActivity")
//            if (chosenActivity != null) {
//                Log.i("0001", chosenActivity)
//            }
//        }
//    }
    override fun onDestroy() {
        super.onDestroy()
        // if app is destroyed, end recording
        recordToggle.isChecked = false    // set record button to not checked to end recording
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent){
        // Receives result from the ChooseActivity activity, and calls beginActivity with that result
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_CHOOSE_ACTIVITY){
            if (resultCode == Activity.RESULT_OK){
                var chosenActivity: String? = data.getStringExtra("chosenActivity")
                Log.i("0001", "$chosenActivity")
                beginActivity("$chosenActivity")
            }
            else{
                Log.i("0001", "error")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        fRaw.write((event.timestamp.toString()+","+
                event.values[0].toString()+","+
                event.values[1].toString()+","+
                event.values[2].toString()+"\n").toByteArray())
    }
    private fun onRecordStart() {
        beginToggle.visibility = View.VISIBLE     // make begin activity button visible
        currentTime = System.currentTimeMillis()
        rawFilename = "$userid.$currentTime.csv"       // file to save raw data

        xmlFilename.text = rawFilename          // set filename for xml

        fRaw = this.openFileOutput(rawFilename, Context.MODE_PRIVATE)
        fRaw.write("Recording Real Start Time: $currentTime\n".toByteArray())
        fRaw.write("timestamp,acc_x,acc_y,acc_z\n".toByteArray())
        mAccel?.also { accel ->
            sensorManager.registerListener(this, accel,
                samplingPeriodMicroseconds, samplingPeriodMicroseconds)
        }
        sessionData = mutableListOf("Session, $currentTime, ")
    }

    private fun onRecordStop() {
        beginToggle.isChecked = false    // set activity button to not checked
        beginToggle.visibility = View.INVISIBLE     // make begin activity button visible

        fRaw.close()
        xmlFilename.text = ""
        sensorManager.unregisterListener(this)

        var endTime = System.currentTimeMillis()
        sessionData[0] += "$endTime"         // write session end time to csv
        writeToSessionCsv()
        fSession.close()
    }

    private fun beginActivity(activityName: String = "Activity") {
        Log.i("0001", "Activity begin")
        var startTime = System.currentTimeMillis()
        sessionData.add("$activityName, $startTime, ")
    }

    private fun endActivity(activityName: String = "Activity") {
        Log.i("0001", "Activity end")
        var endTime = System.currentTimeMillis()
        sessionData[sessionData.size - 1] += "$endTime"
    }

    private fun writeToSessionCsv() {
        sessionFilename = "$userid-session.$currentTime.csv"    // file to save session information

        fSession = this.openFileOutput(sessionFilename, Context.MODE_PRIVATE)
        fSession.write("Event, Start Time, Stop Time\n".toByteArray())
        for(line in sessionData){
            fSession.write("$line\n".toByteArray())
        }
    }
}