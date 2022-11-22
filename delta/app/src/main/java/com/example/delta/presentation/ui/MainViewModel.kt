package com.example.delta.presentation.ui

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key.Companion.D
import androidx.lifecycle.ViewModel
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.delta.util.FilesHandler
import java.time.LocalDateTime
import java.time.LocalTime

class MainViewModel(filesHandler: FilesHandler) : ViewModel() {
    var filesHandler = filesHandler
    var isSmoking by mutableStateOf(false)
    var totalNumberOfPuffsDetected by mutableStateOf(0)
    var totalNumberOfCigsDetected by mutableStateOf(0)
    var numberOfPuffsInCurrentSession by mutableStateOf(0)
    var showConfirmSmokingDialog by mutableStateOf(false)
    var showConfirmDoneSmokingDialog by mutableStateOf(false)
//    var showConfirmReportMissedCigDialog by mutableStateOf(false)
    var allowDialogToBeSent by mutableStateOf(true)

    var missedCigChosenTime = ""
    var missedCigEnjoyment = -1
    var missedCigAction = ""

    fun sendConfirmSmokingDialog(){
        if(allowDialogToBeSent){
            showConfirmSmokingDialog = true
        }
    }
    fun sendConfirmDoneSmokingDialog(){
        if(allowDialogToBeSent){
            showConfirmDoneSmokingDialog = true
        }
    }
    fun onClickSmokingToggleChip(it: Boolean){
        if(it) {
            sendConfirmSmokingDialog()
        } else {
            sendConfirmDoneSmokingDialog()
        }
    }
    fun onConfirmSmokingDialogResponse(response: Boolean){
        showConfirmSmokingDialog = false
        if(response) {
            isSmoking = true
        }
    }
    fun onConfirmDoneSmokingDialogResponse(response: Boolean){
        showConfirmDoneSmokingDialog = false
        if(response) {
            isSmoking = false
            totalNumberOfCigsDetected ++
        }
    }

    fun onClickReportMissedCigChip(){
//        if(allowDialogToBeSent) {
//            allowDialogToBeSent = false
//            showConfirmReportMissedCigDialog = true
//        }
        allowDialogToBeSent = false
    }
//    fun onConfirmReportMissedCigDialogResponse(response: Boolean){
//        showConfirmReportMissedCigDialog = false
//        if(!response) {
//            allowDialogToBeSent = true
//        }
//    }
    fun onTimeConfirmReportMissedCig(chosenTime: LocalTime) {
        Log.d("Delta", "Time: $chosenTime")
        missedCigChosenTime = "$chosenTime"
    }
    fun onConfirmSmokingEnjoyment(enjoyment: Int) {
        Log.d("Delta", "Enjoyment: $enjoyment")
        missedCigEnjoyment = enjoyment
    }
    fun onClickActionPickerChip(action: String){
        Log.d("Delta", "Action: $action")
        allowDialogToBeSent = true
        missedCigAction = action
        filesHandler.writeFalseNegativeToFile(missedCigChosenTime, missedCigEnjoyment, missedCigAction)
    }
    fun onPuffDetected(){
        totalNumberOfPuffsDetected ++
        startPuffTimer(totalNumberOfCigsDetected.toString())
        numberOfPuffsInCurrentSession ++
        if(!isSmoking) {
            if(numberOfPuffsInCurrentSession > 2) {
                showConfirmSmokingDialog = true
            }
        }
    }
    private fun startPuffTimer(id: String): CountDownTimer{
        val timer = object : CountDownTimer(20000, 1000) {
            // 8 minutes of milliseconds is 480000
            override fun onTick(millisUntilFinished: Long) {
                Log.d("0000","Current Puffs: $numberOfPuffsInCurrentSession ID: $id : $millisUntilFinished")
            }
            override fun onFinish() {
                Log.d("0000","20 seconds up! Removing puff from session")
                numberOfPuffsInCurrentSession --
                if(isSmoking && numberOfPuffsInCurrentSession == 0){
                    showConfirmDoneSmokingDialog = true
                }
                cancel()
            }
        }
        timer.start()
        return timer
    }
}