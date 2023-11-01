package com.example.delta.util

import android.hardware.Sensor
import android.hardware.SensorManager

class SensorHandler(fileHandler: FileHandler, sensorManager: SensorManager) {
    private var mSensorManager: SensorManager = sensorManager
    private var mFileHandler: FileHandler = fileHandler
    private val mAccelerometerListener: AccelerometerListener = AccelerometerListener(mFileHandler)
    private val mGyroscopeListener: GyroscopeListener = GyroscopeListener(mFileHandler)
    init {
        val samplingRateHertz = 100
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, samplingPeriodMicroseconds)
        mSensorManager.registerListener(mGyroscopeListener, mGyroscope, samplingPeriodMicroseconds)
    }
    private fun unregisterAccelerometer() {
        mSensorManager.unregisterListener(mAccelerometerListener)
    }
    private fun unregisterGyroscope() {
        mSensorManager.unregisterListener(mGyroscopeListener)
    }
    fun unregisterAll() {
        unregisterAccelerometer()
        unregisterGyroscope()
    }
}