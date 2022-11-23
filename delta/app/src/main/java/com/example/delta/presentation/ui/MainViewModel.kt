package com.example.delta.presentation.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var isSmoking by mutableStateOf(false)
    var totalNumberOfPuffsDetected by mutableStateOf(0)
    var totalNumberOfCigsDetected by mutableStateOf(0)
    var numberOfPuffsInCurrentSession by mutableStateOf(0)
    var showDialog by mutableStateOf(false)
    var allowDialogToBeSent by mutableStateOf(true)
    var mDialogText by mutableStateOf("")
    lateinit var dialogCallback : (Boolean) -> Unit

    private fun sendDialog(dialogText: String){
        Log.d("0000","sending dialog")
        if(allowDialogToBeSent){
            allowDialogToBeSent = false
            showDialog = true
            mDialogText = dialogText
        }
    }
    fun onDialogResponse(res: Boolean){
        Log.d("0000","got dialog response")
        if(!allowDialogToBeSent){
            allowDialogToBeSent = true
            showDialog = false
            dialogCallback(res)
        }
    }

//    fun onClickSmokingToggleChip(it: Boolean){
//        if(it) {
//            sendConfirmSmokingDialog()
//        } else {
//            sendConfirmDoneSmokingDialog()
//        }
//    }

//    fun onConfirmDoneSmokingDialogResponse(response: Boolean){
//        showConfirmDoneSmokingDialog = false
//        if(response) {
//            isSmoking = false
//            totalNumberOfCigsDetected ++
//        }
//    }

    fun onClickReportMissedCigChip(){
        dialogCallback = {
            Log.d("0000","$it")
        }
        sendDialog("Confirm that you want to report missed cig.")
    }
//    fun onClickActivityPickerChip(it: String){
//        Log.d("0000",it)
//    }
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
}