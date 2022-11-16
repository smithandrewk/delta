package com.example.delta.presentation.ui

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var isSmokingState by mutableStateOf(false)
    var numberOfPuffsState by mutableStateOf(0)
    var numberOfCigsState by mutableStateOf(0)
    var numberOfPuffsInCurrentSession by mutableStateOf(0)
    var alertShowDialog by mutableStateOf(false)
    var showConfirmDoneSmokingDialog by mutableStateOf(false)

    fun onPuffDetected(){
        numberOfPuffsState ++
        startPuffTimer(numberOfPuffsState.toString())
        numberOfPuffsInCurrentSession ++
        if(!isSmokingState) {
            if(numberOfPuffsInCurrentSession > 2) {
                alertShowDialog = true
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
                if(isSmokingState && numberOfPuffsInCurrentSession == 0){
                    showConfirmDoneSmokingDialog = true
                }
                cancel()
            }
        }
        timer.start()
        return timer
    }
}