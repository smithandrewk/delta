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
                    val filesDir: File,
                    writeToLogFile: (logEntry: String) -> Unit,
                    writeToEventsFile: (event_id: Int) -> Unit,
                    writeStopSessionToEventsFile: (event_id: Int, satisfaction: Int) -> Unit,
                    writeFalseNegativeToEventsFile: (event_id: Int,
                                                     dateTimeForUserInput: String,
                                                     satisfaction: Int,
                                                     otherActivity: String) -> Unit,
                    writeFalsePositiveToEventsFile: (event_id: Int, otherActivity: String) -> Unit,
                    navigateToFnSlider: () -> Unit,
                    navigateToFpActivitiesList: () -> Unit

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
    var mNavigateToFnSlider: () -> Unit = navigateToFnSlider
    var mWriteToLogFile: (logEntry: String) -> Unit = writeToLogFile
    var mWriteToEventsFile: (events_id: Int) -> Unit = writeToEventsFile
    var mWriteFalseNegativeToEventsFile: (event_id: Int, dateTimeForUserInput: String, satisfaction: Int, otherActivity: String) -> Unit = writeFalseNegativeToEventsFile
    var mWriteFalsePositiveToEventsFile: (event_id: Int, otherActivity: String) -> Unit = writeFalsePositiveToEventsFile
    var mWriteStopSessionToEventsFile: (event_id: Int, satisfaction: Int) -> Unit = writeStopSessionToEventsFile
    var mNavigateToFpActivitiesList: () -> Unit = navigateToFpActivitiesList

    // False Negative Activities
    private val fnActivitiesFile = File(filesDir, "fnActivities")
    var fnActivities by mutableStateOf(mutableListOf(""))

    // False Positive Activities
    private val fpActivitiesFile = File(filesDir, "fpActivities")
    var fpActivities by mutableStateOf(mutableListOf(""))

    var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")
    var dateTimeForUserInput: LocalDateTime by mutableStateOf(LocalDateTime.now())
    var satisfaction by mutableStateOf(5)
    var otherActivity by mutableStateOf("")
    var puffTimers = mutableListOf<CountDownTimer>()
    var currentDestination: String = ""
    var lastResponse: String = "dismiss"
    var lastDialogWasTimedOut = false
    var dialogQueued: Boolean = false
    lateinit var mQueuedOnDialogResponse : (Boolean) -> Unit
    var mQueuedDialogText by mutableStateOf("")

    init {
        fnActivities = initializeActivitiesFile(fnActivitiesFile, "reclining\ndriving\nLaying\n")
        fpActivities = initializeActivitiesFile(fpActivitiesFile, "eating\nvaping\ndrinking\n")
    }

    private fun initializeActivitiesFile(file: File, activitiesList: String): MutableList<String> {
        // if /data/data/com.example.delta/activities exists
        if(file.exists()){
            // do nothing
        } else {
            // if activities file doesn't exist, write simple list of activities to file
            file.writeText(activitiesList)
        }
        // save activities to mutable list as well
        return file.readLines().toMutableList()
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
        sendDialog(applicationContext.getString(R.string.confirm_ai_detected_session),
            onDialogResponse = { response ->
                if(response){
                    startSmoking(R.integer.AI_START_SMOKING)
                }
                else if (!lastDialogWasTimedOut) {
                    onRejectDetectedPuff()
                    lastDialogWasTimedOut = false
                }
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
        sendDialog(
            applicationContext.getString(R.string.confirm_done_session_dialog_text),
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
            lastDialogWasTimedOut = true
            onDialogResponse("dismiss")
        }
    }

    // PUFF AND SESSION DETECTION CONTROL
    private fun resetSessionTimer(){
        mWriteToLogFile("resetSessionTimer")
        sessionTimer.cancel()
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
        mNavigateToFnSlider()
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

    fun onClickCigSliderScreenButton(it: Int) {
        mWriteToLogFile("onClickCigSliderScreenButton $it")
        satisfaction = it
    }

    // False Positives

    fun onSubmitNewFpActivity(activity: String) {
        mWriteToLogFile("OnSubmitNewFPActivity")
        fpActivitiesFile.appendText("$activity\n")
        fpActivities.add(activity)
    }
    fun onRejectDetectedPuff() {
        mWriteToLogFile("onRejectDetectedPuff")
        mNavigateToFpActivitiesList()
    }
    fun onClickFPActivityButton(it: String){
        mWriteToLogFile("onClickFPActivityButton $it")
        mWriteFalsePositiveToEventsFile(
            R.integer.FALSE_POSITIVE,
            it // chosen activity
        )
    }


    // False Negatives

    fun onSubmitNewFNActivity(activity: String){
        mWriteToLogFile("onSubmitNewFNActivity $activity")
        fnActivitiesFile.appendText("$activity\n")
        fnActivities.add(activity)
    }

    fun onClickReportMissedCigChip(navigateToTimePicker: () -> Unit){
        mWriteToLogFile("onClickReportMissedCigChip")
        sendDialog(applicationContext.getString(R.string.report_false_negative_dialog_text),
            onDialogResponse = { if(it) navigateToTimePicker() })
    }
    fun onFnTimePickerConfirm(it: LocalTime){
        mWriteToLogFile("onFnTimePickerConfirm $it")
        dateTimeForUserInput = it.atDate(dateTimeForUserInput.toLocalDate())
    }
    fun onClickFnSliderScreenButton(it: Int) {
        mWriteToLogFile("onClickFnSliderScreenButton $it")
        satisfaction = it
    }
    fun onClickFNActivityButton(it: String){
        mWriteToLogFile("onClickFNActivityButton $it")
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
