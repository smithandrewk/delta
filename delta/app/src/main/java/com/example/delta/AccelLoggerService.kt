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


class AccelLoggerService: Service(), SensorEventListener {
    private lateinit var activityChangeReceiver: ActivityChangeReceiver
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private val NOTIFICATION_CHANNEL_ID = "Channel_1"
    private val samplingRateHertz = 100

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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
            NOTIFICATION_CHANNEL_ID,
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

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
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
                // Do stuff - maybe update my view based on the changed DB contents
                Log.i("0003", "${intent.getStringExtra("ACTIVITY")}")
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