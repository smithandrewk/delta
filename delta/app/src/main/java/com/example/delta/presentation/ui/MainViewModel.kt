package com.example.delta.presentation.ui

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.example.delta.R
import java.io.File
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MainViewModel(vibrateWatch: () -> Unit,
                    val applicationContext: Context,
                    writeToLogFile: (logEntry: String) -> Unit,
                    writeToEventsFile: (event_id: Int) -> Unit,
                    writeStopSessionToEventsFile: (event_id: Int, satisfaction: Int) -> Unit,
                    writeFalseNegativeToEventsFile: (event_id: Int,
                                                     dateTimeForUserInput: String,
                                                     satisfaction: Int,
                                                     otherActivity: String) -> Unit,
                    navigateToSlider: () -> Unit
) : ViewModel() {
    // Timer Lengths
    val sessionTimerLengthMilliseconds: Long = 480000
    val dialogTimerLengthMilliseconds: Long = 20000

    // State variables
    var isSmoking by mutableStateOf(false)
    var totalNumberOfPuffsDetected by mutableStateOf(0)
    var totalNumberOfCigsDetected by mutableStateOf(0)
    var numberOfPuffsInCurrentSession by mutableStateOf(0)
    var showDialog by mutableStateOf(false)
    var alertStatus by mutableStateOf("")
    private var allowDialogToBeSent by mutableStateOf(true)
    var mDialogText by mutableStateOf("")
    var secondarySmokingText by mutableStateOf("tap to start")
    var sessionLengthSeconds by mutableStateOf(0)

    // Local Copies of functions
    lateinit var mOnDialogResponse : (Boolean) -> Unit
    var mVibrateWatch: () -> Unit = vibrateWatch
    var mNavigateToSlider: () -> Unit = navigateToSlider
    var mWriteToLogFile: (logEntry: String) -> Unit = writeToLogFile
    var mWriteToEventsFile: (events_id: Int) -> Unit = writeToEventsFile
    var mWriteFalseNegativeToEventsFile: (event_id: Int, dateTimeForUserInput: String, satisfaction: Int, otherActivity: String) -> Unit = writeFalseNegativeToEventsFile
    var mWriteStopSessionToEventsFile: (event_id: Int, satisfaction: Int) -> Unit = writeStopSessionToEventsFile

    private val activitiesFile = File(applicationContext.filesDir, "activities")
    var activities by mutableStateOf(mutableListOf(""))
    var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")
    var dateTimeForUserInput: LocalDateTime by mutableStateOf(LocalDateTime.now())
    var satisfaction by mutableStateOf(5)
    var otherActivity by mutableStateOf("")
    var puffTimers = mutableListOf<CountDownTimer>()
    var currentDestination: String = ""
    var lastResponse: String = "dismiss"
    var dialogQueued: Boolean = false
    lateinit var mQueuedOnDialogResponse : (Boolean) -> Unit
    var mQueuedDialogText by mutableStateOf("")

    init {
        initializeActivitiesFile()
    }

    private fun initializeActivitiesFile(){
        // if /data/data/com.example.delta/activities exists
        if(activitiesFile.exists()){
            // do nothing
        } else {
            // if activities file doesn't exist, write simple list of activities to file
            activitiesFile.writeText("reclining\nvaping\neating\ndriving\n")
        }
        // save activities to mutable list as well
        activities = activitiesFile.readLines().toMutableList()
    }

    // DIALOG CONTROL
    private fun sendDialog(dialogText: String, onDialogResponse: (Boolean) -> Unit){
        mWriteToLogFile("sendDialog($dialogText)")
        // General send dialog called by specific use-case functions
        if(!allowDialogToBeSent) {
            mWriteToLogFile("dialog cannot be sent, queuing for after response")
            dialogQueued = true
            mQueuedOnDialogResponse = onDialogResponse
            mQueuedDialogText = dialogText
            return
        }
        mOnDialogResponse = onDialogResponse
        mDialogText = dialogText
        alertStatus = ""
        allowDialogToBeSent = false
        mVibrateWatch()
        showDialog = true
        dialogTimer.start()
    }

    fun onDialogResponse(res: String){
        mWriteToLogFile("onDialogResponse($res)")
        alertStatus = res
        if(res == "dismiss") {
            mOnDialogResponse(false)
        } else {
            mOnDialogResponse(res != "no")
        }
        lastResponse = res
        closeDialog()
    }
    private fun closeDialog(){
        mWriteToLogFile("closeDialog")
        showDialog = false
        allowDialogToBeSent = true
        dialogTimer.cancel()
        if(dialogQueued){
            sendDialog(mQueuedDialogText,mQueuedOnDialogResponse)
            dialogQueued = false
        }
    }
    private fun sendConfirmSmokingDialog(){
        mWriteToLogFile("sendConfirmSmokingDialog")
        sendDialog("Confirm that you started smoking.",
            onDialogResponse = { response ->
                if(response) startSmoking(R.integer.AI_START_SMOKING)
                for (timer in puffTimers) {
                    timer.cancel()
                }
                puffTimers.clear()
                numberOfPuffsInCurrentSession = 0
                               }
        )
    }
    private fun sendConfirmDoneSmokingDialog() {
        mWriteToLogFile("sendConfirmDoneSmokingDialog")
        sendDialog("Confirm that you are done smoking.",
            onDialogResponse = { response ->
                Log.d("0001","Confirm Done Smoking Response: $response")
                if(response) {
                    stopSmoking(R.integer.TIMER_STOP_SMOKING)
                    for (timer in puffTimers) {
                        timer.cancel()
                    }
                    puffTimers.clear()
                    numberOfPuffsInCurrentSession = 0
                } else {
                    resetSessionTimer()
                }
                               }
        )
    }
    private val dialogTimer = object : CountDownTimer(dialogTimerLengthMilliseconds, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            Log.d("0001","dialogTimer $millisUntilFinished")
        }
        override fun onFinish() {
            Log.d("0001","dialogTimer::onFinish dialog timed out after 20 seconds")
            // If dialog times out, same as dismissing
            onDialogResponse("dismiss")
        }
    }

    // PUFF AND SESSION DETECTION CONTROL
    private fun resetSessionTimer(){
        mWriteToLogFile("resetSessionTimer")
        sessionTimer.cancel()
        sessionLengthSeconds = 0
        sessionTimer.start()
    }
    private fun startPuffTimer(){
        mWriteToLogFile("startPuffTimer")
        val timer = object : CountDownTimer(sessionTimerLengthMilliseconds, 60000) {
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
    private val sessionTimer = object : CountDownTimer(sessionTimerLengthMilliseconds, 1000) {
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

    // RESPONSE TO UI
    private fun startSmoking(source: Int){
        mWriteToLogFile("startSmoking")
        mWriteToEventsFile(source)
        isSmoking = true
        sessionTimer.start()
    }
    private fun stopSmoking(source: Int){
        mWriteToLogFile("stopSmoking")
        mWriteStopSessionToEventsFile(source, satisfaction)
        sessionLengthSeconds = 0
        secondarySmokingText = "tap to start"
        totalNumberOfCigsDetected ++
        isSmoking = false
        sessionTimer.cancel()
        mNavigateToSlider()
    }
    fun onClickSmokingToggleChip(){
        mWriteToLogFile("onClickSmokingToggleChip")
        if(isSmoking){
            stopSmoking(R.integer.USER_STOP_SMOKING)
        } else {
            startSmoking(R.integer.USER_START_SMOKING)
        }
    }
    fun onDestinationChangedCallback(destination: NavDestination){
        currentDestination = "${destination.route}"
//        allowDialogToBeSent = currentDestination == "landing"
    }
    fun onSubmitNewActivity(activity: String){
        mWriteToLogFile("onSubmitNewActivity $activity")
        activitiesFile.appendText("$activity\n")
        activities.add(activity)
    }

    // False Negatives
    fun onClickReportMissedCigChip(navigateToTimePicker: () -> Unit){
        mWriteToLogFile("onClickReportMissedCigChip")
        sendDialog("Confirm that you want to report missed cig.",
            onDialogResponse = { if(it) navigateToTimePicker() })
    }
    fun onTimePickerConfirm(it: LocalTime){
        mWriteToLogFile("onTimePickerConfirm $it")
        dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
    }
    fun onClickSliderScreenButton(it: Int) {
        mWriteToLogFile("onClickSliderScreenButton $it")
        satisfaction = it
    }
    fun onClickCigSliderScreenButton(it: Int) {
        mWriteToLogFile("onClickCigSliderScreenButton $it")
        satisfaction = it
    }
    fun onClickActivityButton(it: String){
        mWriteToLogFile("onClickActivityButton $it")
        otherActivity = it
        mWriteFalseNegativeToEventsFile(R.integer.FALSE_NEGATIVE,
                                        dateTimeForUserInput.format(dateTimeFormatter),
                                        satisfaction,
                                        otherActivity)
        totalNumberOfCigsDetected ++
    }

    // AI
    fun onPuffDetected(){
        mWriteToLogFile("onPuffDetected")
        mWriteToEventsFile(R.integer.PUFF_DETECTED)
        totalNumberOfPuffsDetected ++
        if (isSmoking) return
        numberOfPuffsInCurrentSession ++
        startPuffTimer()
        if(numberOfPuffsInCurrentSession > 2) {
            mWriteToEventsFile(R.integer.SESSION_DETECTED)
            sendConfirmSmokingDialog()
        }
    }
}
