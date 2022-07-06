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
import com.example.delta.databinding.ActivityMainBinding
import java.io.FileOutputStream

class MainActivity : Activity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var f: FileOutputStream
    private val samplingRateHertz = 100
    private val samplingRateMicroseconds = samplingRateHertz*100
    private var mAccel: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        f = this.openFileOutput("out.csv", Context.MODE_PRIVATE)

        val recordToggle: ToggleButton = findViewById(R.id.activityToggleButton)
        recordToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onRecordStart()
                Log.i("0001", "on")
            } else {
                onRecordStop()
                Log.i("0001", "off")
            }
        }

        val beginToggle: ToggleButton = findViewById(R.id.beginToggleButton)
        beginToggle.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked){
                beginActivity()
            } else {
                endActivity()
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.i("0001", "Logging")
        f.write((event.timestamp.toString()+","+
                event.values[0].toString()+","+
                event.values[1].toString()+","+
                event.values[2].toString()+"\n").toByteArray())
    }
    private fun onRecordStart() {
        Log.i("0001", "Start")
        f = this.openFileOutput("out.csv", Context.MODE_PRIVATE)
        f.write("timestamp,x,y,z,test,est\n".toByteArray())
        mAccel?.also { accel ->
            sensorManager.registerListener(this, accel,
                samplingRateMicroseconds, samplingRateMicroseconds)
        }
    }

    private fun onRecordStop() {
        Log.i("0001", "Stop")
        f.close()
        sensorManager.unregisterListener(this)
    }

    private fun beginActivity() {
        
    }
}