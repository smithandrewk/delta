package com.example.delta.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Log

class AccelerometerListener(mSensorManager: SensorManager,
                            mPowerManager: PowerManager,
                            private var filesHandler: FilesHandler
): SensorEventListener {

    private var mSensorThread: HandlerThread = HandlerThread("Sensor thread", Thread.MAX_PRIORITY)
    private var mSensorHandler: Handler
    private var mAccelerometer: Sensor
    private var mWakeLock: PowerManager.WakeLock
    init {
        Log.d("0000","AccelerometerListener:init")
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "delta:daddy")
        mWakeLock.acquire()
        mSensorThread.start()
        mSensorHandler = Handler(mSensorThread.looper) //Blocks until looper is prepared, which is fairly quick
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST, mSensorHandler)
    }
    override fun onSensorChanged(p0: SensorEvent?) {
        /*
            timestamp : The time in nanoseconds at which the event happened.
            For a given sensor, each new sensor event should be monotonically
            increasing using the same time base as
            android.os.SystemClock#elapsedRealtimeNanos()
            android.os.SystemClock#elapsedRealtimeNanos(): Returns
            nanoseconds since boot, including time spent in sleep.
         */
        if (p0 != null) {
            filesHandler.writeStringToRawFile("${p0.timestamp},${p0.values[0]},${p0.values[1]},${p0.values[2]}\n")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }
}