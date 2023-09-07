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
    private val applicationContext = applicationContext
    private val filesHandler = filesHandler
    private val mViewModel = mViewModel
    private var neuralHandler: NeuralHandler

    // Record raw data
    private var mSensorManager: SensorManager = sensorManager
    private var sampleIndex: Int = 0
    private val numWindowsBatched = applicationContext.resources.getInteger(R.integer.NUM_WINDOWS_BATCHED)
    private var xBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var yBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var zBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var extrasBuffer:MutableList<MutableList<String>> = mutableListOf()
    private val windowUpperLim = numWindowsBatched + 99
    private val windowRange:IntRange = numWindowsBatched..windowUpperLim

    init {
        neuralHandler = getNeuralHandler()
        val samplingRateHertz = 100
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        mSensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (sampleIndex == 5) {
            sampleIndex = 0
            xBuffer.add(mutableListOf(event.values[0].toDouble()))
            yBuffer.add(mutableListOf(event.values[1].toDouble()))
            zBuffer.add(mutableListOf(event.values[2].toDouble()))
            extrasBuffer.add(
                mutableListOf(
                    event.timestamp.toString(),
                )
            )
            if (xBuffer.size > windowUpperLim) {
                neuralHandler.processBatch(extrasBuffer, xBuffer, yBuffer, zBuffer)

                // clear buffer
                xBuffer = xBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                yBuffer = yBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                zBuffer = zBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                extrasBuffer = extrasBuffer.slice(windowRange) as MutableList<MutableList<String>>
            }
        }
        sampleIndex++
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }
    fun unregister() {
        mSensorManager.unregisterListener(this)
    }
    private fun getNeuralHandler(): NeuralHandler{
        // Load ANN weights and input ranges
        var ins: InputStream = applicationContext.resources.openRawResource(R.raw.input_to_hidden_weights_and_biases)
        val inputToHiddenWeightsAndBiasesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        ins = applicationContext.resources.openRawResource(R.raw.hidden_to_output_weights_and_biases)
        val hiddenToOutputWeightsAndBiasesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        ins = applicationContext.resources.openRawResource(R.raw.input_ranges)
        val inputRangesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        return NeuralHandler(
            inputToHiddenWeightsAndBiasesString,
            hiddenToOutputWeightsAndBiasesString,
            inputRangesString,
            numWindowsBatched,
            filesHandler, mViewModel
        )
    }
}