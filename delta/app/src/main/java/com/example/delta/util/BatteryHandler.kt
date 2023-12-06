package com.example.delta.util
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.activity.ComponentActivity
import kotlin.reflect.KFunction1

class BatteryHandler(registerReceiver: (receiver: BroadcastReceiver, filter: IntentFilter, flags: Int) -> Unit, unregisterReceiver: (br: BroadcastReceiver) -> Unit, fileHandler: FileHandler, updateBatteryLevel: (newLevel: Float) -> Unit, setIsChargingState: KFunction1<Int, Unit>){
    private val br: BroadcastReceiver = BatteryBroadcastReceiver(fileHandler,updateBatteryLevel, setIsChargingState)
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
    class BatteryBroadcastReceiver (fileHandler: FileHandler, updateBatteryLevel: (newLevel: Float) -> Unit, setIsChargingState: (newState: Int) -> Unit): BroadcastReceiver() {
        private val mFileHandler = fileHandler
        private val mUpdateBatteryLevel = updateBatteryLevel
        private val setIsChargingState = setIsChargingState
        override fun onReceive(context: Context, intent: Intent) {
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL
            val batteryLevel = level * 100 / scale.toFloat()
            mFileHandler.writeToLog("battery: $batteryLevel")
//            setIsChargingState(if(isCharging) 1 else 0)
            mUpdateBatteryLevel(batteryLevel)
        }
    }
}