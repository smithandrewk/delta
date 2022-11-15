package com.example.delta.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.delta.presentation.ui.MainViewModel

class SensorHandler(applicationContext: Context, filesHandler: FilesHandler, mViewModel: MainViewModel, sensorManager: SensorManager) : SensorEventListener {
    private val applicationContext = applicationContext
    private val filesHandler = filesHandler
    private val mViewModel = mViewModel
    private var currentActivity = "None"

    // Record raw data
    private lateinit var sensorManager: SensorManager
    private var sampleIndex: Int = 0
    private val numWindowsBatched = 1
    private var xBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var yBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var zBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var extrasBuffer:MutableList<MutableList<String>> = mutableListOf()
    private val windowUpperLim = numWindowsBatched + 99
    private val windowRange:IntRange = numWindowsBatched..windowUpperLim

    // TODO Neural Handler

    init {
        val samplingRateHertz = 100
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)
    }

    override fun onSensorChanged(event: SensorEvent) {
//        filesHandler.writeToRawFile("")
        Log.v("onSensorChanged", "Time: ${event.timestamp}    x: ${event.values[0]}     y: ${event.values[1]}    z: ${event.values[2]}")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }

    fun onIsSmokingToggleClicked() {
        mViewModel.isSmokingState = !mViewModel.isSmokingState
        Log.i("Delta","SensorManager.onIsSmokingToggleClicked() : isSmoking = ${mViewModel.isSmokingState}")
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }
}