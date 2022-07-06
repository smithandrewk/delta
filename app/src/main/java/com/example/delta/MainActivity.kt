package com.example.delta

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.os.Bundle
import android.util.Log
import android.widget.ToggleButton
import androidx.wear.widget.CurvedTextView
import com.example.delta.databinding.ActivityMainBinding
import java.io.FileOutputStream

class MainActivity : Activity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var f: FileOutputStream
    private val samplingRateHertz = 1
    private val samplingPeriodSeconds = 1/samplingRateHertz
    private val samplingPeriodMicroseconds = samplingPeriodSeconds * 1000000
    private var mAccel: Sensor? = null

    private lateinit var filename: CurvedTextView
    private lateinit var samplingFrequency: CurvedTextView
    private var currentTime = System.currentTimeMillis()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        samplingFrequency = findViewById(R.id.samplingFrequency)
        filename = findViewById(R.id.filename)

        samplingFrequency.text = "$samplingRateHertz Hz"

        val toggle: ToggleButton = findViewById(R.id.activityToggleButton)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onStartButton()
            } else {
                onStopButton()
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        f.write((event.timestamp.toString()+","+
                event.values[0].toString()+","+
                event.values[1].toString()+","+
                event.values[2].toString()+"\n").toByteArray())
    }
    private fun onStartButton() {
        currentTime = System.currentTimeMillis()
        filename.text = "$currentTime.csv"

        f = this.openFileOutput("$currentTime.csv", Context.MODE_PRIVATE)
        f.write("timestamp,acc_x,acc_y,acc_z\n".toByteArray())
        mAccel?.also { accel ->
            sensorManager.registerListener(this, accel,
                samplingPeriodMicroseconds, samplingPeriodMicroseconds)
        }
    }

    private fun onStopButton() {
        f.close()
        filename.text = ""
        sensorManager.unregisterListener(this)
    }
}