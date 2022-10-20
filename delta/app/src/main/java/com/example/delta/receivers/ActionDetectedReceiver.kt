//package com.example.delta.receivers
//
//import android.app.*
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.util.Log
//import android.widget.Toast
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationCompat.Action.SEMANTIC_ACTION_THUMBS_UP
//import com.example.delta.R
//
//
//class ActionDetectedReceiver : BroadcastReceiver() {
//    // Inner class to define the broadcast receiver
//    // This Broadcast Receiver receives signals from AccelLoggerService when smoking is detected
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == context.getString(R.string.ACTIVITY_DETECTED_BROADCAST_CODE)) {
//            val detectedActivity = intent.getStringArrayListExtra(context.getString(R.string.ACTIVITY))
//
//            if (detectedActivity != null) {
//                Log.i("Action Detected Receiver", "Received")
//                Toast.makeText(context, "Yes clicked", Toast.LENGTH_SHORT).show()
//                val dialogClickListener =
//                    DialogInterface.OnClickListener { dialog, which ->
//
//                        when (which) {
//                            // on below line we are setting a click listener
//                            // for our positive button
//                            DialogInterface.BUTTON_POSITIVE -> {
//                                // on below line we are displaying a toast message.
//                                Toast.makeText(context, "Yes clicked", Toast.LENGTH_SHORT).show()
//                            }
//
//                            // on below line we are setting click listener
//                            // for our negative button.
//                            DialogInterface.BUTTON_NEGATIVE -> {
//                                // on below line we are dismissing our dialog box.
//                                dialog.dismiss()
//                            }
//                        }
//                    }
//                // on below line we are creating a builder variable for our alert dialog
//                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//
//                // on below line we are setting message for our dialog box.
//                builder.setMessage("Puff ?")
//                    // on below line we are setting positive
//                    // button and setting text to it.
//                    .setPositiveButton("Yes", dialogClickListener)
//                    // on below line we are setting negative button
//                    // and setting text to it.
//                    .setNegativeButton("No", dialogClickListener)
//                    // on below line we are calling
//                    // show to display our dialog.
//                    .show()
////                for(activity in detectedActivity){ //TODO check that app is in MainActivity
////                    Log.i("ActionDetectedReceiver", "Detected: $activity")
////                    val mChannel = NotificationChannel(
////                        context.getString(R.string.NOTIFICATION_CHANNEL_2_ID),
////                        "activity_alert_channel",
////                        NotificationManager.IMPORTANCE_HIGH
////                    )
////                    mChannel.description = "Channel to display notifications about detecting activities"
////                    var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////                    notificationManager.createNotificationChannel(mChannel)
////
//////                    val smokingConfirmedIntent = Intent(context, ActivityConfirmedReceiver::class.java).apply {
//////                        action = context.getString(R.string.ACTIVITY_RESPONSE_BROADCAST_CODE)
//////                        putExtra("smoking_confirmed_id", 0)
//////                    }
////                    val smokingConfirmedPintent: PendingIntent =
////                        PendingIntent.getBroadcast(context,
////                            0,
////                            Intent(context.getString(R.string.ACTIVITY_DETECTED_BROADCAST_CODE)),
////                            0)
////
////                    val builder = NotificationCompat.Builder(context, context.getString(R.string.NOTIFICATION_CHANNEL_2_ID))
////                        .setContentTitle("Delta")
////                        .setContentText("Are you smoking?")
////                        .setSmallIcon(R.drawable.ic_smoking)
////                        .setContentIntent(null)     // Don't open any activity when Notification is clicked
////                        .addAction(SEMANTIC_ACTION_THUMBS_UP, "Yes", smokingConfirmedPintent)
////                    notificationManager.notify(1234, builder.build())
////                    // TODO start activity (use snackbar) if user says yes (and not in activity now)
////                }
//            }
//        }
//    }
//}