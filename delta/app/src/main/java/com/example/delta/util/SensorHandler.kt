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
    private val numWindowsBatched = 1
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
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)

    }

    override fun onSensorChanged(event: SensorEvent) {
        if (sampleIndex == 5) {
            sampleIndex = 0
            xBuffer.add(mutableListOf(event.values[0].toDouble()))
            yBuffer.add(mutableListOf(event.values[1].toDouble()))
            zBuffer.add(mutableListOf(event.values[2].toDouble()))
            extrasBuffer.add(mutableListOf(
                event.timestamp.toString(),
                Calendar.getInstance().timeInMillis.toString(),
                if(mViewModel.isSmoking) "Smoking" else "None"
            ))
            if(xBuffer.size > windowUpperLim){
                neuralHandler.processBatch(extrasBuffer, xBuffer, yBuffer, zBuffer)

                // clear buffer
                xBuffer = xBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                yBuffer = yBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                zBuffer = zBuffer.slice(windowRange) as MutableList<MutableList<Double>>
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
        // Load ANN weights and input ranges
        // TODO: Can we move loading the weights to the NeuralHandler class?
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
            "big boy",
            inputToHiddenWeightsAndBiasesString,
            hiddenToOutputWeightsAndBiasesString,
            inputRangesString,
            numWindowsBatched,applicationContext,filesHandler,mViewModel)
    }
}