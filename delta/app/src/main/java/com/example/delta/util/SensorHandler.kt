package com.example.delta.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.delta.R
import com.example.delta.presentation.ui.MainViewModel
import java.io.InputStream

class SensorHandler(applicationContext: Context, filesHandler: FilesHandler, mViewModel: MainViewModel, sensorManager: SensorManager) : SensorEventListener {
    private val filesHandler = filesHandler
    private var mSensorManager: SensorManager = sensorManager

    init {
        val samplingRateHertz = 100
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        mSensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)

    }

    override fun onSensorChanged(event: SensorEvent) {
        filesHandler.writeEventToRawFile(eventTimeStamp = event.timestamp,
            x = event.values[0],
            y = event.values[1],
            z = event.values[2])
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }
    fun unregister() {
        mSensorManager.unregisterListener(this)
    }
}