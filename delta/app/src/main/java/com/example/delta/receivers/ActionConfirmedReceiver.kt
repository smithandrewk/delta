package com.example.delta.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.delta.R

class ActionConfirmedReceiver : BroadcastReceiver() {
    // Inner class to define the broadcast receiver
    // This Broadcast Receiver receives response of user to confirm detected activity from notification
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == context.getString(R.string.ACTIVITY_RESPONSE_BROADCAST_CODE)) {
            val activityResponse = intent.getBooleanExtra(context.getString(R.string.ACTIVITY_RESPONSE), false)
            Log.i("0001", "Response: $activityResponse")
            if(activityResponse){
                // TODO start EndActivityButton
            }
        }
    }
}