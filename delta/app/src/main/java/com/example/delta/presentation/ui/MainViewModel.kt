package com.example.delta.presentation.ui

import android.content.Context
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination
import com.example.delta.presentation.navigation.Screen
import com.example.delta.util.FilesHandler
import java.io.File
import java.time.LocalDateTime
import java.time.LocalTime

class MainViewModel(vibrateWatch: () -> Unit,
                    applicationContext: Context,
                    writeFalseNegativeToFile: (dateTimeForUserInput: LocalDateTime,
                                               satisfaction: Int,
                                               otherActivity: String) -> Unit,) : ViewModel() {
    val sessionLengthMilliseconds: Long = 10000
    var isSmoking by mutableStateOf(false)
    var totalNumberOfPuffsDetected by mutableStateOf(0)
    var totalNumberOfCigsDetected by mutableStateOf(0)
    var numberOfPuffsInCurrentSession by mutableStateOf(0)
    var showDialog by mutableStateOf(false)
    private var allowDialogToBeSent by mutableStateOf(true)
    var mDialogText by mutableStateOf("")
    var secondarySmokingText by mutableStateOf("tap to start")
    var sessionLengthSeconds by mutableStateOf(0)
    lateinit var mOnDialogResponse : (Boolean) -> Unit
    var mVibrateWatch: () -> Unit = vibrateWatch
    var mWriteFalseNegativeToFile: (dateTimeForUserInput: LocalDateTime, satisfaction: Int, otherActivity: String) -> Unit = writeFalseNegativeToFile
    private val file = File(applicationContext.filesDir, "activities")
    var activities by mutableStateOf(mutableListOf(""))
    var dateTimeForUserInput: LocalDateTime by mutableStateOf(LocalDateTime.now())
    var satisfaction by mutableStateOf(5)
    var otherActivity by mutableStateOf("")
    var puffTimers = mutableListOf<CountDownTimer>()
    var currentDestination: String = ""
    lateinit var mOnDismissDialogRequest : () -> Unit
    var lastResponse: String = "dismiss"


    init {
        initializeActivitiesFile()
    }
    private fun initializeActivitiesFile(){
        // if /data/data/com.example.delta/activities exists
        if(file.exists()){
            // do nothing
        } else {
            // if activities file doesn't exist, write simple list of activities to file
            file.writeText("reclining\nvaping\neating\ndriving\n")
        }
        // save activities to mutable list as well
        activities = file.readLines().toMutableList()
    }
    private fun sendDialog(dialogText: String, onDialogResponse: (Boolean) -> Unit, onDismissDialogRequest: () -> Unit){
        // General send dialog called by specific use-case functions
        if(!allowDialogToBeSent) return
        mOnDialogResponse = onDialogResponse
        mOnDismissDialogRequest = onDismissDialogRequest
        mDialogText = dialogText
        allowDialogToBeSent = false
        mVibrateWatch()
        showDialog = true
        dialogTimer.start()
    }

    fun onDialogResponse(res: String){
        Log.d("0001","onDialogResponse $res")
        if(lastResponse == "dismiss" && res == "dismiss"){
            // just a dismiss
            mOnDialogResponse(false)
        } else if (lastResponse == "dismiss" && res != "dismiss"){
            // actual response
            mOnDialogResponse(res != "no")
        } else {
            // ignore
        }
        lastResponse = res
        closeDialog()
    }
    private fun closeDialog(){
        showDialog = false
        allowDialogToBeSent = true
        dialogTimer.cancel()
    }
    private fun sendConfirmSmokingDialog(){
        sendDialog("Confirm that you started smoking.",
            onDialogResponse = { response ->
                if(response) startSmoking()
                for (timer in puffTimers) {
                    timer.cancel()
                }
                puffTimers.clear()
                numberOfPuffsInCurrentSession = 0
                               },
            onDismissDialogRequest = {
                // onDismissDialogRequest from user, assume response is negative
                mOnDialogResponse(false)
            }
        )
    }
    private fun sendConfirmDoneSmokingDialog() {
        sendDialog("Confirm that you are done smoking.",
            onDialogResponse = { response ->
                Log.d("0001","onDialogResponse")
                if(response) {
                    stopSmoking()
                    for (timer in puffTimers) {
                        timer.cancel()
                    }
                    puffTimers.clear()
                    numberOfPuffsInCurrentSession = 0
                } else {
                    resetSessionTimer()
                }
                               },
            onDismissDialogRequest = {
                Log.d("0001","onDismissDialog")
                // onDismissDialogRequest from user, assume response is negative
                mOnDialogResponse(false)
            }
        )
    }
    private fun resetSessionTimer(){
        sessionTimer.cancel()
        sessionLengthSeconds = 0
        sessionTimer.start()
    }
    private fun startPuffTimer(){
        val timer = object : CountDownTimer(480000, 60000) {
            // 8 minutes of milliseconds is 480000
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                // puff was more than 8 minutes ago
                numberOfPuffsInCurrentSession --
                puffTimers.removeAt(0)
                cancel()
            }
        }
        puffTimers.add(timer)
        timer.start()
    }
    private val sessionTimer = object : CountDownTimer(sessionLengthMilliseconds, 1000) {
                // 8 minutes of milliseconds is 480000
                override fun onTick(millisUntilFinished: Long) {
                    // onTick is called every countDownInterval, which we force to be 1000 ms;
                    // thus, iterate sessionLengthSeconds by 1 every time onTick is called
                    sessionLengthSeconds ++
                    // Given a variable with seconds, formats as a string mm:ss
                    secondarySmokingText = "${(sessionLengthSeconds / 60).toString().padStart(2, '0')} : ${(sessionLengthSeconds % 60).toString().padStart(2, '0')}"
                }
                override fun onFinish() {
                    // onFinish is only called when millisUntilFinished equals 0, never called externally
                    sendConfirmDoneSmokingDialog()
                }
            }
    private val dialogTimer = object : CountDownTimer(20000, 10000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                Log.d("0000","dialogTimer::onFinish dialog timed out after 20 seconds")
                // If dialog times out, same as dismissing
                onDialogResponse("dismiss")
                cancel()
            }
        }
    private fun startSmoking(){
        isSmoking = true
        sessionTimer.start()
    }
    private fun stopSmoking(){
        sessionLengthSeconds = 0
        secondarySmokingText = "tap to start"
        totalNumberOfCigsDetected ++
        isSmoking = false
        sessionTimer.cancel()
    }
    fun onClickSmokingToggleChip(){
        if(isSmoking){
            stopSmoking()
        } else {
            startSmoking()
        }
    }
    fun onDestinationChangedCallback(destination: NavDestination){
        currentDestination = "${destination.route}"
        allowDialogToBeSent = currentDestination == "landing"
    }
    fun onSubmitNewActivity(activity: String){
        file.appendText("$activity\n")
        activities.add(activity)
    }
    fun onClickReportMissedCigChip(navigateToTimePicker: (Boolean) -> Unit){
        sendDialog("Confirm that you want to report missed cig.",
            onDialogResponse = navigateToTimePicker,
            onDismissDialogRequest = { closeDialog() })
    }
    fun onTimePickerConfirm(it: LocalTime){
        dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
    }
    fun onClickSliderScreenButton(it: Int) {
        satisfaction = it
    }
    fun onClickActivityButton(it: String){
        otherActivity = it
        mWriteFalseNegativeToFile(dateTimeForUserInput, satisfaction, otherActivity)
        totalNumberOfCigsDetected ++
    }
    fun onPuffDetected(){
        totalNumberOfPuffsDetected ++
        if (isSmoking) return
        numberOfPuffsInCurrentSession ++
        startPuffTimer()
        if(numberOfPuffsInCurrentSession > 2) {
            sendConfirmSmokingDialog()
        }
    }
}
