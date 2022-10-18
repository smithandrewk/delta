package com.example.delta

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService


class ActionDetectedReceiver : BroadcastReceiver() {
    // Inner class to define the broadcast receiver
    // This Broadcast Receiver receives signals from AccelLoggerService when smoking is detected
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == context.getString(R.string.ACTIVITY_DETECTED_BROADCAST_CODE)) {
            val detectedActivity = intent.getStringArrayListExtra(context.getString(R.string.ACTIVITY))

            if (detectedActivity != null) {
                for(activity in detectedActivity){ //TODO check that app is in MainActivity
                    Log.i("0001", "Detected: $activity")
                    val mChannel = NotificationChannel(
                        context.getString(R.string.NOTIFICATION_CHANNEL_2_ID),
                        "activity_alert_channel",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    mChannel.description = "Channel to display notifications about detecting activities"
                    var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(mChannel)

//                    val smokingConfirmedIntent = Intent(context, ActivityConfirmedReceiver::class.java).apply {
//                        action = context.getString(R.string.ACTIVITY_RESPONSE_BROADCAST_CODE)
//                        putExtra("smoking_confirmed_id", 0)
//                    }
//                    val smokingConfirmedPendingIntent: PendingIntent =
//                        PendingIntent.getBroadcast(context, 0, smokingConfirmedIntent, 0)

                    val builder = Notification.Builder(context, context.getString(R.string.NOTIFICATION_CHANNEL_2_ID))
                        .setContentTitle("Delta")
                        .setContentText("Are you smoking?")
                        .setSmallIcon(R.drawable.ic_smoking)
                        .setContentIntent(null)     // Don't open any activity when Notification is clicked
//                        .addAction(0, "Yes", smokingConfirmedPendingIntent)
                    notificationManager.notify(1234, builder.build())
                    // TODO start activity (use snackbar) if user says yes (and not in activity now)
                }
            }
        }
    }
}