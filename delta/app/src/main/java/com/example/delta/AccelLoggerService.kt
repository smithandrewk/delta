package com.example.delta

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import java.util.*


class AccelLoggerService: Service(), SensorEventListener {
    private lateinit var activityChangeReceiver: ActivityChangeReceiver
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private val samplingRateHertz = 100
    private val LAUNCH_END_BUTTON_CODE = 2
    private lateinit var chosenActivity: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        chosenActivity = getString(R.string.NO_ACTIVITY)

        val intentFilter = IntentFilter(getString(R.string.BROADCAST_CODE))
        activityChangeReceiver = ActivityChangeReceiver()
        registerReceiver(activityChangeReceiver, intentFilter)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)

        // Start foreground service
        startForeground(1, createNotification())

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        var x = event.values[0]
        var y = event.values[1]
        var z = event.values[2]
        var time = event.timestamp
        Log.v("service", "time: $time    x: $x     y: $y    z: $z")
    }
    private fun createNotification(): Notification {
        // Create the NotificationChannel
        val channelName = "foreground_service_channel"
        val channelDescription = "Notifications for foreground service"
        val mChannel = NotificationChannel(
            getString(R.string.NOTIFICATION_CHANNEL_1_ID),
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        mChannel.description = channelDescription
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        // Create Notification
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        return Notification.Builder(this, getString(R.string.NOTIFICATION_CHANNEL_1_ID))
            .setContentTitle("Delta")
            .setContentText("Accelerometer Recording")
            .setSmallIcon(R.drawable.ic_pizza_outline)
            .setContentIntent(pendingIntent)
            .setTicker("Ticker")
            .build()
    }
    private inner class ActivityChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == getString(R.string.BROADCAST_CODE)) {
                val chosenActivity = intent.getStringExtra(getString(R.string.ACTIVITY))
                if(chosenActivity == getString(R.string.NO_ACTIVITY)){
                    Log.i("0003", "Ended $chosenActivity}")

                }
                else{
                    Log.i("0003", "Started $chosenActivity}")
                    
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.i("service", "DESTROYED")
        sensorManager.unregisterListener(this)
//        activityChangeReceiver. unregister
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }
}