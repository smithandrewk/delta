package com.example.delta.util

import android.util.Log
import com.example.delta.presentation.ui.MainViewModel
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class FilesHandler(filesDir: File, mViewModel: MainViewModel, appStartTimeMillis: Long, appStartTimeReadable: String) {
    private val filesDir = filesDir
    private val mViewModel = mViewModel
    private val appStartTimeMillis = appStartTimeMillis
    private val appStartTimeReadable = appStartTimeReadable

    // Files
    private lateinit var dataFolderName: String
    private var rawFileIndex: Int = 0
    private lateinit var fRaw: FileOutputStream         // File output stream to write raw acc data
    private lateinit var falseNegativesFile: File       // File to write false negative events
    private lateinit var eventsFile: File               // File to write smoking events
    private lateinit var positivesFile: File            // File to write smoking detected events
    // TODO positive puffs

    private val numWindowsBatched = 1       // TODO should we get rid of this? or at least make it global

    init {
        createInitialFiles()
    }

    fun writeFalseNegativeToFile(dateTimeForUserInput: LocalDateTime) {
    falseNegativesFile.appendText("${Calendar.getInstance().timeInMillis},${dateTimeForUserInput}\n")
    }

    private fun createInitialFiles(){
        // Create folder for this session's files
        dataFolderName = appStartTimeReadable
        File(filesDir, dataFolderName).mkdir()
        createNewRawFile()

        // Event Recording Files
        // TODO add source of end
        eventsFile = File(this.filesDir, "$dataFolderName/Self-Report.$dataFolderName.csv")
        eventsFile.appendText("Event,Start Time,Stop Time\n")

        falseNegativesFile = File(this.filesDir, "$dataFolderName/False-Negatives.$dataFolderName.csv")
        falseNegativesFile.appendText("timeInMillis,userEstimatedTimeOfFalseNegative\n")

        positivesFile = File(this.filesDir, "$dataFolderName/Positives.$dataFolderName.csv")
        positivesFile.appendText("Time \n")

        // Info File
        try {
            val json = JSONObject()
                .put("App Start Time", appStartTimeMillis)
                .put("App Start Time Readable", appStartTimeReadable)
                .put("Number of Windows Batched", numWindowsBatched)
            File(this.filesDir, "$dataFolderName/Info.json").appendText(json.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }
    // Setup Functions
    fun createNewRawFile() {
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
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity,label,state\n".toByteArray())
        rawFileIndex++
    }
    fun writeToRawFile(data: String){
        // TODO
    }

    fun closeRawFile(){
        fRaw.close()
    }
}