package com.example.delta.util

import android.content.Context
import android.util.Log
import com.example.delta.R
import com.example.delta.presentation.ui.MainViewModel
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.*

class FilesHandler(private val applicationContext: Context,
                   private val filesDir: File,
                   val mViewModel: MainViewModel,
                   private val appStartTimeMillis: Long,
                   private val appStartTimeReadable: String) {

    // Files
    private lateinit var dataFolderName: String
    private var rawFileIndex: Int = 0
    private lateinit var fRaw: FileOutputStream         // File output stream to write raw acc data
    private lateinit var fLog: FileOutputStream         // File to write smoking detected events
    private lateinit var fEvents: FileOutputStream                 // File to write all events

    private val numWindowsBatched = 1       // TODO should we get rid of this? or at least make it global

    private val eventIDs = mapOf(
        R.integer.FALSE_NEGATIVE to "False Negative Reported",
        R.integer.PUFF_DETECTED to "Puff Detected",
        R.integer.SESSION_DETECTED to "Session Detected",
        R.integer.USER_START_SMOKING to "User Started Smoking Session",
        R.integer.AI_START_SMOKING to "AI Started Smoking Session",
        R.integer.USER_STOP_SMOKING to "User Stopped Smoking Session",
        R.integer.TIMER_STOP_SMOKING to "Timer Stopped Smoking Session"
    )

    init {
        createInitialFiles()
    }

    private fun createInitialFiles(){
        // Create folder for this session's files
        dataFolderName = appStartTimeReadable
        File(filesDir, dataFolderName).mkdir()
        createNewRawFile()

        fLog = FileOutputStream(File(this.filesDir, "$dataFolderName/log.csv"))

        fEvents = FileOutputStream(File(this.filesDir, "$dataFolderName/events.csv"))
        fEvents.write("time,event_id,event,extras\n".toByteArray())   // time in ms, id of event, name of event, and any extras

        // Info File
        try {
            val json = JSONObject()
                .put("App Start Time", appStartTimeMillis)
                .put("App Start Time Readable", appStartTimeReadable)
                .put("Number of Windows Batched", numWindowsBatched)
                // TODO put watch model, and other data
            File(this.filesDir, "$dataFolderName/Info.json").appendText(json.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Setup Functions
    private fun createNewRawFile() {
        // Create a new raw file for accelerometer data
        Log.i("0003", "Creating New Raw File")
        if (rawFileIndex == 0) {
            // Create "raw" directory
            File(this.filesDir, "$dataFolderName/raw").mkdir()
        }
        else {
            fRaw.close()
        }
        val rawFilename = "$appStartTimeReadable.$rawFileIndex.csv"
        fRaw = FileOutputStream(File(this.filesDir, "$dataFolderName/raw/$rawFilename"))
        fRaw.write("File Start Time: ${Calendar.getInstance().timeInMillis}\n".toByteArray())
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity,label,rawlabel,state\n".toByteArray())
        rawFileIndex++
    }
    fun writeToRawFile(eventTimeStamp: String, acc_x: Double,acc_y: Double,acc_z: Double,timeInMillis: String,smokingStateString: String,thresholdSmokingOutput: Double,rawSmokingOutput: Double,expertStateMachineState: Int){
        fRaw.write("${eventTimeStamp},${acc_x},${acc_y},${acc_z},${timeInMillis},${smokingStateString},${thresholdSmokingOutput},${rawSmokingOutput},${expertStateMachineState}\n".toByteArray())
    }
    fun writeToLogFile(logEntry: String){
        fLog.write("${Calendar.getInstance().timeInMillis}: $logEntry\n".toByteArray())
    }
    fun writeToEventsFile(event_id: Int) {
        // Write any event with no extras
        fEvents.write(("${Calendar.getInstance().timeInMillis}," +
                "${applicationContext.resources.getInteger(event_id)}," +
                "${eventIDs[event_id]}," +
                "null\n").toByteArray())
    }
    fun writeNegativesToEventsFile(event_id: Int, dateTime: String, satisfaction: Int, otherActivity: String){
        // write time, the id of the event, corresponding name of the event, and any extra parameters
        fEvents.write(("${Calendar.getInstance().timeInMillis}," +
                "${applicationContext.resources.getInteger(event_id)}," +
                "${eventIDs[event_id]},").toByteArray())
        // Add extras as json object
        try {
            val json = JSONObject()
                .put("dateTime", dateTime)
                .put("satisfaction", satisfaction)
                .put("otherActivity", otherActivity)
            fEvents.write((json.toString()+"\n").toByteArray())
        } catch (e: Exception) { e.printStackTrace() }
    }
    fun writeStringToRawFile(string: String){
        fRaw.write(string.toByteArray())
    }
    fun closeRawFile(){
        fRaw.close()
    }
}