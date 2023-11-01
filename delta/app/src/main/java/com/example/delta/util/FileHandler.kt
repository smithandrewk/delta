package com.example.delta.util

import android.hardware.SensorEvent
import android.icu.text.SimpleDateFormat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FileHandler(filesDir: File) {
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(
        Date()
    )
    private var fLog: FileOutputStream
    private var fAccelerometerData: FileOutputStream
    private var fGyroscopeData: FileOutputStream
    private val mFilesDir = filesDir
    init {
        File(mFilesDir, appStartTimeReadable).mkdir()
        fLog = FileOutputStream(File(filesDir, "$appStartTimeReadable/log.csv"))
        fLog.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
        fLog.write("timestamp,method\n".toByteArray())
        fAccelerometerData = FileOutputStream(File(filesDir, "$appStartTimeReadable/acceleration.csv"))
        fAccelerometerData.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
        fAccelerometerData.write("timestamp,x,y,z\n".toByteArray())
        fGyroscopeData = FileOutputStream(File(filesDir, "$appStartTimeReadable/gyroscope.csv"))
        fGyroscopeData.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
        fGyroscopeData.write("timestamp,x,y,z\n".toByteArray())
        try {
            val json = JSONObject()
                .put("App Start Time Readable", appStartTimeReadable)
            // TODO put watch model, and other data
            File(mFilesDir, "$appStartTimeReadable/info.json").appendText(json.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun writeToLog(msg:String){
        fLog.write("${android.icu.util.Calendar.getInstance().timeInMillis},$msg\n".toByteArray())
    }
    fun writeAccelerometerEvent(event: SensorEvent){
        fAccelerometerData.write("${event.timestamp},${event.values[0]},${event.values[1]},${event.values[2]}\n".toByteArray())
    }
    fun writeGyroscopeEvent(event: SensorEvent){
        fGyroscopeData.write("${event.timestamp},${event.values[0]},${event.values[1]},${event.values[2]}\n".toByteArray())
    }
    fun closeFiles(){
        fLog.close()
        fAccelerometerData.close()
        fGyroscopeData.close()
    }
}