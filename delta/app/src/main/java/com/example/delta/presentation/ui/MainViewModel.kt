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

class MainViewModel(vibrateWatch: () -> Unit, applicationContext: Context,writeFalseNegativeToFile: (dateTimeForUserInput: LocalDateTime, satisfaction: Int, otherActivity: String) -> Unit,) : ViewModel() {
    var isSmoking by mutableStateOf(false)
    var totalNumberOfPuffsDetected by mutableStateOf(0)
    var totalNumberOfCigsDetected by mutableStateOf(0)
    var numberOfPuffsInCurrentSession by mutableStateOf(0)
    var showDialog by mutableStateOf(false)
    var allowDialogToBeSent by mutableStateOf(true)
    var mDialogText by mutableStateOf("")
    var secondarySmokingText by mutableStateOf("tap to start")
    var sessionLengthSeconds by mutableStateOf(0)
    lateinit var mDialogCallback : (Boolean) -> Unit
    var mVibrateWatch: () -> Unit = vibrateWatch
    var mWriteFalseNegativeToFile: (dateTimeForUserInput: LocalDateTime, satisfaction: Int, otherActivity: String) -> Unit = writeFalseNegativeToFile

    private val file = File(applicationContext.filesDir, "activities")
    var activities by mutableStateOf(mutableListOf(""))
    var dateTimeForUserInput: LocalDateTime by mutableStateOf(LocalDateTime.now())
    var satisfaction by mutableStateOf(5)
    var otherActivity by mutableStateOf("")
    init {
        Log.d("0000","instanced file")
        if(file.exists()){
            Log.d("0000","file exists")
        } else {
            Log.d("0000","file does not exist")
            file.writeText("reclining\nvaping\neating\ndriving\n")
        }
        activities = file.readLines().toMutableList()
    }
    private fun sendDialog(dialogText: String, dialogCallback: (Boolean) -> Unit){
        Log.d("0000","sending dialog")
        mDialogCallback = dialogCallback
        if(allowDialogToBeSent){
            allowDialogToBeSent = false
            mDialogText = dialogText
            mVibrateWatch()
            showDialog = true
            dialogTimer.start()
        }
    }
    fun dismissDialog(){
        Log.d("0000","on dismiss request")
        if(!allowDialogToBeSent){
            allowDialogToBeSent = true
            showDialog = false
            dialogTimer.cancel()
        }
    }
    fun onDialogResponse(res: Boolean){
        Log.d("0000","got dialog response")
        mDialogCallback(res)
        dismissDialog()
    }
    private fun startSmoking(){
        sessionTimer.start()
    }
    private fun stopSmoking(){
        sessionTimer.onFinish()
    }
    fun onClickSmokingToggleChip(){
        if(isSmoking){
            stopSmoking()
        } else {
            startSmoking()
        }
        isSmoking = !isSmoking
    }
    fun onPuffDetected(){
        totalNumberOfPuffsDetected ++
    }
    fun onDestinationChangedCallback(destination: NavDestination){
        Log.d("0000","$destination")
    }
    fun onSubmitNewActivity(activity: String){
        file.appendText("$activity\n")
        activities.add(activity)
    }
    fun onClickReportMissedCigChip(navigateToTimePicker: (Boolean) -> Unit){
        Log.d("0000","onClickReportMissedCigChip")
        sendDialog("Confirm that you want to report missed cig.", dialogCallback = navigateToTimePicker)
    }
    fun onTimePickerConfirm(it: LocalTime){
        Log.d("0000","onTimePickerConfirm $it")
        dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
    }
    fun onClickSliderScreenButton(it: Int) {
        Log.d("0000","onClickSliderScreenButton $it")
        satisfaction = it
    }
    fun onClickActivityButton(it: String){
        Log.d("0000","onClickActivityButton $it")
        otherActivity = it
        mWriteFalseNegativeToFile(dateTimeForUserInput, satisfaction, otherActivity)
    }

//    fun onConfirmReportMissedCigDialogResponse(response: Boolean){
//        showConfirmReportMissedCigDialog = false
//        if(!response) {
//            allowDialogToBeSent = true
//        }
//    }
//    fun onPuffDetected(){
//        totalNumberOfPuffsDetected ++
//        startPuffTimer(totalNumberOfCigsDetected.toString())
//        numberOfPuffsInCurrentSession ++
//        if(!isSmoking) {
//            if(numberOfPuffsInCurrentSession > 2) {
//                showConfirmationDialog = true
//            }
//        }
//    }
//    private fun startPuffTimer(id: String): CountDownTimer{
//        val timer = object : CountDownTimer(20000, 1000) {
//            // 8 minutes of milliseconds is 480000
//            override fun onTick(millisUntilFinished: Long) {
//                Log.d("0000","Current Puffs: $numberOfPuffsInCurrentSession ID: $id : $millisUntilFinished")
//            }
//            override fun onFinish() {
//                Log.d("0000","20 seconds up! Removing puff from session")
//                numberOfPuffsInCurrentSession --
//                if(isSmoking && numberOfPuffsInCurrentSession == 0){
//                    showConfirmationDialog = true
//                }
//                cancel()
//            }
//        }
//        timer.start()
//        return timer
//    }
        val sessionTimer = object : CountDownTimer(480000, 1000) {
                // 8 minutes of milliseconds is 480000
                override fun onTick(millisUntilFinished: Long) {
                    sessionLengthSeconds ++
                    secondarySmokingText = "${(sessionLengthSeconds / 60).toString().padStart(2, '0')} : ${(sessionLengthSeconds % 60).toString().padStart(2, '0')}"
                }
                override fun onFinish() {
                    Log.d("0000","20 seconds up! Removing puff from session")
                    sessionLengthSeconds = 0
                    secondarySmokingText = "tap to start"
                    cancel()
                }
            }
        private val dialogTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
//                Log.d("0000","$millisUntilFinished")
            }
            override fun onFinish() {
                Log.d("0000","dialogTimer::onFinish dialog timed out after 20 seconds")
                dismissDialog()
                cancel()
            }
        }
    }

//    fun onConfirmDoneSmokingDialogResponse(response: Boolean){
//        showConfirmDoneSmokingDialog = false
//        if(response) {
//            isSmoking = false
//            totalNumberOfCigsDetected ++
//        }
//    }