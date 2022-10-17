package com.example.delta

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.delta.databinding.ActivityMainBinding
import java.util.*

class MainActivity : Activity() {
    private lateinit var accelIntent: Intent

    private val launchEndButtonCode = 1
    private lateinit var binding: ActivityMainBinding

    private val activityOptions = mapOf(R.id.eatButton to "Eating",
                                        R.id.drinkButton to "Drinking",
                                        R.id.smokeButton to "Smoking",
                                        R.id.otherButton to "Other")

    private val activitiesCount = mutableMapOf("Smoking" to 0)
    private lateinit var activityDetectedReceiver: MainActivity.ActivityDetectedReceiver
    private lateinit var activityConfirmedReceiver: MainActivity.ActivityConfirmedReceiver
    private lateinit var mApp: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("0001", "CREATED")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mApp = this.applicationContext as Application

        // start service to record accelerometer data
        accelIntent = Intent(applicationContext, AccelLoggerService::class.java)
        startForegroundService(accelIntent)

        // get chosen activity from user - create onClickListener for each button
        activityOptions.forEach { (button, chosenActivity) ->
            findViewById<Button>(button).setOnClickListener { startNewActivity(chosenActivity) }
        }

        createBroadcastReceiver()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Receives result from the EndActivityButton activity, and notifies accelerometer service
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == launchEndButtonCode) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("0001", "Signalled Service - End Activity")
                // tell service that activity has ended
                sendBroadcast(Intent(getString(R.string.ACTIVITY_CHANGE_BROADCAST_CODE))
                    .putExtra(getString(R.string.ACTIVITY), getString(R.string.NO_ACTIVITY)))
            }
            else {
                Log.i("0001", "Error Receiving Activity Result")
            }
        }
    }
    private fun startNewActivity(chosenActivity: String){
        Log.i("0001", "Signalled Service - Started $chosenActivity")
        // tell service that new activity is starting
        sendBroadcast(Intent(getString(R.string.ACTIVITY_CHANGE_BROADCAST_CODE))
            .putExtra(getString(R.string.ACTIVITY), chosenActivity))

        // start EndActivityButton activity
        val endButtonIntent = Intent(this, EndActivityButton::class.java)
        startActivityForResult(endButtonIntent, launchEndButtonCode)
    }
    private fun createBroadcastReceiver() {
        // Create and register instance of broadcast receiver to receive signals from MainActivity
        activityDetectedReceiver = ActivityDetectedReceiver()
        registerReceiver(activityDetectedReceiver,
            IntentFilter(getString(R.string.ACTIVITY_DETECTED_BROADCAST_CODE))
        )
        activityConfirmedReceiver = ActivityConfirmedReceiver()
        registerReceiver(activityConfirmedReceiver,
            IntentFilter(getString(R.string.ACTIVITY_RESPONSE_BROADCAST_CODE))
        )
    }
    private inner class ActivityDetectedReceiver : BroadcastReceiver() {
        // Inner class to define the broadcast receiver
        // This Broadcast Receiver receives signals from AccelLoggerService when smoking is detected
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == getString(R.string.ACTIVITY_DETECTED_BROADCAST_CODE)) {
                val detectedActivity = intent.getStringArrayListExtra(getString(R.string.ACTIVITY))

                if (detectedActivity != null) {
                    for(activity in detectedActivity){ //TODO check that app is in MainActivity
                        Log.i("0001", "Detected: $activity")
                        Toast.makeText(applicationContext, activity, Toast.LENGTH_SHORT).show()
                        val mChannel = NotificationChannel(
                            getString(R.string.NOTIFICATION_CHANNEL_2_ID),
                            "activity_alert_channel",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        mChannel.description = "Channel to display notifications about detecting activities"
                        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(mChannel)

//                        val smokingConfirmedIntent = Intent(this@MainActivity, ActivityConfirmedReceiver::class.java).apply {
//                            action = getString(R.string.ACTIVITY_RESPONSE_BROADCAST_CODE)
//                            putExtra("smoking_confirmed_id", 0)
//                        }
//                        val smokingConfirmedPendingIntent: PendingIntent =
//                            PendingIntent.getBroadcast(this@MainActivity, 0, smokingConfirmedIntent, 0)

                        val builder = Notification.Builder(this@MainActivity, getString(R.string.NOTIFICATION_CHANNEL_2_ID))
                        .setContentTitle("Delta")
                            .setContentText("Are you smoking?")
                            .setSmallIcon(R.drawable.ic_smoking)
                            .setContentIntent(null)     // Don't open any activity when Notification is clicked
//                            .addAction(0, "Yes", smokingConfirmedPendingIntent)
                        notificationManager.notify(1234, builder.build())
                    // TODO start activity (use snackbar) if user says yes (and not in activity now)
                    }
                }

            }
        }
    }
    private inner class ActivityConfirmedReceiver : BroadcastReceiver() {
        // Inner class to define the broadcast receiver
        // This Broadcast Receiver receives response of user to confirm detected activity from notification
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == getString(R.string.ACTIVITY_RESPONSE_BROADCAST_CODE)) {
                val activityResponse = intent.getBooleanExtra(getString(R.string.ACTIVITY_RESPONSE), false)
                Log.i("0001", "Response: $activityResponse")
                if(activityResponse){
                    // TODO start EndActivityButton
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.i("0001", "DESTROYED")
        // When app is destroyed, stop the service
        stopService(accelIntent)
        unregisterReceiver(activityDetectedReceiver)
        unregisterReceiver(activityConfirmedReceiver)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("0001", "Saved instance")
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i("0001", "Restored instance")
    }
    override fun onStop() { super.onStop(); Log.i("0001", "STOPPED") }
    override fun onPause() { super.onPause(); Log.i("0001", "PAUSED") }
    override fun onStart() { super.onStart(); Log.i("0001", "STARTED") }
    override fun onRestart() { super.onRestart(); Log.i("0001", "RESTARTED") }
    override fun onResume() { super.onResume(); Log.i("0001", "RESUMED") }
}