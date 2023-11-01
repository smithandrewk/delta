package com.example.delta.util
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.activity.ComponentActivity

class BatteryHandler (registerReceiver: (receiver: BroadcastReceiver, filter: IntentFilter, flags: Int) -> Unit, unregisterReceiver: (br: BroadcastReceiver) -> Unit, fileHandler: FileHandler, updateBatteryLevel: (newLevel: Float) -> Unit){
    private val br: BroadcastReceiver = BatteryBroadcastReceiver(fileHandler,updateBatteryLevel)
    private val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    private val listenToBroadcastsFromOtherApps = false
    private val receiverFlags = if (listenToBroadcastsFromOtherApps) {
        ComponentActivity.RECEIVER_EXPORTED
    } else {
        ComponentActivity.RECEIVER_NOT_EXPORTED
    }
    private val mUnregisterReceiver = unregisterReceiver
    init {
        registerReceiver(br, filter, receiverFlags)
    }
    fun unregister(){
        mUnregisterReceiver(br)
    }
    class BatteryBroadcastReceiver (fileHandler: FileHandler, updateBatteryLevel: (newLevel: Float) -> Unit): BroadcastReceiver() {
        private val mFileHandler = fileHandler
        private val mUpdateBatteryLevel = updateBatteryLevel
        override fun onReceive(context: Context, intent: Intent) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryLevel = level * 100 / scale.toFloat()
            mFileHandler.writeToLog("battery: $batteryLevel")
            mUpdateBatteryLevel(batteryLevel)
        }
    }
}