package com.example.delta.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.delta.R
import com.example.delta.presentation.ui.MainViewModel
import java.io.InputStream
import java.util.*

class SensorHandler(applicationContext: Context, filesHandler: FilesHandler, mViewModel: MainViewModel, sensorManager: SensorManager) : SensorEventListener {
    private val applicationContext = applicationContext
    private val filesHandler = filesHandler
    private val mViewModel = mViewModel
    private var neuralHandler: NeuralHandler

    // Record raw data
    private lateinit var sensorManager: SensorManager
    private var sampleIndex: Int = 0
    private val numWindowsBatched = applicationContext.resources.getInteger(R.integer.NUM_WINDOWS_BATCHED)
    private var xBuffer:MutableList<MutableList<Float>> = mutableListOf()
    private var yBuffer:MutableList<MutableList<Float>> = mutableListOf()
    private var zBuffer:MutableList<MutableList<Float>> = mutableListOf()
    private var extrasBuffer:MutableList<MutableList<String>> = mutableListOf()
    private val windowUpperLim = numWindowsBatched + 99
    private val windowRange:IntRange = numWindowsBatched..windowUpperLim

    init {
        neuralHandler = getNeuralHandler()

        val samplingRateHertz = 100
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (sampleIndex == 5) {
            sampleIndex = 0
            xBuffer.add(mutableListOf(event.values[0]))
            yBuffer.add(mutableListOf(event.values[1]))
            zBuffer.add(mutableListOf(event.values[2]))
            extrasBuffer.add(mutableListOf(
                event.timestamp.toString(),
                Calendar.getInstance().timeInMillis.toString(),
                if(mViewModel.isSmoking) "Smoking" else "None"
            ))
            if(xBuffer.size > windowUpperLim){
                neuralHandler.processBatch(extrasBuffer, xBuffer, yBuffer, zBuffer)

                // clear buffer
                xBuffer = xBuffer.slice(windowRange) as MutableList<MutableList<Float>>
                yBuffer = yBuffer.slice(windowRange) as MutableList<MutableList<Float>>
                zBuffer = zBuffer.slice(windowRange) as MutableList<MutableList<Float>>
                extrasBuffer = extrasBuffer.slice(windowRange)  as MutableList<MutableList<String>>
            }
//        Log.v("onSensorChanged", "Time: ${event.timestamp}    x: ${event.values[0]}     y: ${event.values[1]}    z: ${event.values[2]}    smoking: ${mViewModel.isSmokingState}")
        }
        sampleIndex++
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }

    fun onIsSmokingToggleClicked() {
        Log.i("Delta","SensorManager.onIsSmokingToggleClicked() : isSmoking = ${mViewModel.isSmoking}")
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }
    private fun getNeuralHandler(): NeuralHandler{
        return NeuralHandler(
            "big Pytorch boy",
            "model.pt",
            numWindowsBatched,
            applicationContext,
            filesHandler,
            mViewModel)
    }
}