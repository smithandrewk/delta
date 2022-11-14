package com.example.delta.presentation

import android.graphics.Color.parseColor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.example.delta.R
import com.example.delta.presentation.theme.DeltaTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

class MainActivity : ComponentActivity(), SensorEventListener {
    private val appStartTimeReadable = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private val appStartTimeMillis = Calendar.getInstance().timeInMillis

    // Files
    private lateinit var dataFolderName: String
    private var rawFileIndex: Int = 0
    private lateinit var fRaw: FileOutputStream         // File output stream to write raw acc data
    private lateinit var falseNegativesFile: File       // File to write false negative events
    private lateinit var eventsFile: File               // File to write smoking events
    private lateinit var positivesFile: File            // File to write smoking detected events
    // TODO positive puffs

    // Record raw data
    private lateinit var sensorManager: SensorManager
    private var sampleIndex: Int = 0
    private val numWindowsBatched = 1
    private var xBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var yBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var zBuffer:MutableList<MutableList<Double>> = mutableListOf()
    private var extrasBuffer:MutableList<MutableList<String>> = mutableListOf()
    private val windowUpperLim = numWindowsBatched + 99
    private val windowRange:IntRange = numWindowsBatched..windowUpperLim

    // Neural Network
    private lateinit var nHandler: NeuralHandler
    private var currentActivity: String = "None"
    var isSmoking: Boolean = false

    // UI
    val mViewModel: MainViewModel = MainViewModel()
    lateinit var timer: CountDownTimer
    private val sessionLengthMillis: Long = 10000
    private val progressIndicatorIterator: Float = 0.1f
    private var currentTimerProgress: Long = 0
    private val countDownIntervalMillis: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContent {
            val uiState by mViewModel.uiState.collectAsState()
            val animatedProgress by animateFloatAsState(
                targetValue = uiState.progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            WearApp(uiState)
            ReportMissedCigDialog(
                showDialog = uiState.showConfirmReportFalseNegativeDialog,
                onDialogResponse = {
                    mViewModel.setShowConfirmReportFalseNegativeDialog(false)
                    if(it) {
                        mViewModel.iterateNumberOfCigs()
                        onReportFalseNegative()
                    }
                })
            ConfirmSmokeACigDialog(
                showDialog = uiState.showConfirmSmokingDialog,
                onDialogResponse = {
                    mViewModel.setShowConfirmSmokingDialog(false)
                    Log.d("0000",uiState.isSmoking.toString())
                    if(it) startSmoking(sessionLengthMillis,0.0f)
                })
            ConfirmDoneSmokingDialog(
                showDialog = uiState.showConfirmDoneSmokingDialog,
                onDialogResponse = {
                    mViewModel.setShowConfirmDoneSmokingDialog(false)
                    if(it) stopSmoking()
                })
            if(uiState.isSmoking){
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize(),
                    startAngle = 290.0f,
                    endAngle = 250.0f,
                    strokeWidth = 4.dp
                )
            }
            SideEffect {
                isSmoking = uiState.isSmoking
            }
        }

        createInitialFiles()
        createAndRegisterAccelerometerListener()
        nHandler = getNeuralHandler(this)
    }

    // Functions to setup and listen to accelerometer sensor
    private fun createAndRegisterAccelerometerListener() {
        // Register Listener for Accelerometer Data
        val samplingRateHertz = 100
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        sensorManager.registerListener(this, sensor, samplingPeriodMicroseconds)
    }
    override fun onSensorChanged(event: SensorEvent) {
        /*
            We observed experimentally Ticwatch E samples at 100 Hz consistently for 9 hours
            in our app. Therefore, we take every 5th value from onsensorchanged to approximate
            20 Hz sampling rate.
         */
//        mViewModel.updateSensorData("(${event.values[0]}, ${event.values[1]}, ${event.values[2]})")
        mViewModel.updateSensorData(event.values[0].toString(), event.values[1].toString(), event.values[2].toString())
        if (sampleIndex == 5){
            sampleIndex = 0
            xBuffer.add(mutableListOf(event.values[0].toDouble()))
            yBuffer.add(mutableListOf(event.values[1].toDouble()))
            zBuffer.add(mutableListOf(event.values[2].toDouble()))
            extrasBuffer.add(mutableListOf(
                event.timestamp.toString(),
                Calendar.getInstance().timeInMillis.toString(),
                if(isSmoking) "Smoking" else "None"
            ))
            if(xBuffer.size > windowUpperLim){
                nHandler.processBatch(extrasBuffer, xBuffer, yBuffer, zBuffer, fRaw)

                // clear buffer
                xBuffer = xBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                yBuffer = yBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                zBuffer = zBuffer.slice(windowRange) as MutableList<MutableList<Double>>
                extrasBuffer = extrasBuffer.slice(windowRange)  as MutableList<MutableList<String>>
            }
            Log.v("onSensorChanged","x: ${xBuffer.size}     y: ${yBuffer.size}    z: ${zBuffer.size}, extras: ${extrasBuffer.size}")
//            Log.v("onSensorChanged", "Time: ${event.timestamp}    x: ${event.values[0]}     y: ${event.values[1]}    z: ${event.values[2]}, activity: $currentActivity")
        }
        sampleIndex++
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }

    // Functions to respond to Neural Network and User
    fun startSmoking(millisInFuture:Long,progressIndicatorProgress: Float){
        mViewModel.setIsSmoking(true)
        mViewModel.setProgress(progressIndicatorProgress)

        Log.d("0000","Starting UI timer for $millisInFuture")
        timer = object : CountDownTimer(millisInFuture, countDownIntervalMillis) {
            override fun onTick(millisUntilFinished: Long) {
                mViewModel.iterateProgressByFloat(progressIndicatorIterator)
                Log.d("0000","Current timer progress : $currentTimerProgress")
                currentTimerProgress += countDownIntervalMillis
            }

            override fun onFinish() {
                Log.d("0000", "smoking timer on finish")
                stopSmoking()
            }
        }
        timer.start()

        // HERE write to event file as "Self Report" or "Detected"
    }
    fun stopSmoking(){
        mViewModel.setIsSmoking(false)
        mViewModel.iterateNumberOfCigs()
        currentTimerProgress = 0
        timer.cancel()

        // HERE write to event file
    }

    private fun onReportFalseNegative(){
        falseNegativesFile.appendText("${Calendar.getInstance().timeInMillis}\n")
    }
    private fun createInitialFiles(){
        currentActivity = getString(R.string.NO_ACTIVITY)

        // Create folder for this session's files
        dataFolderName = appStartTimeReadable
        File(this.filesDir, dataFolderName).mkdir()
        createNewRawFile()

        // Event Recording Files
        // TODO add source of end
        eventsFile = File(this.filesDir, "$dataFolderName/Self-Report.$dataFolderName.csv")
        eventsFile.appendText("Event,Start Time,Stop Time\n")

        falseNegativesFile = File(this.filesDir, "$dataFolderName/False-Negatives.$dataFolderName.csv")
        falseNegativesFile.appendText("Time\n")

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
        fRaw.write("timestamp,acc_x,acc_y,acc_z,real time,activity,label,state\n".toByteArray())
        rawFileIndex++
    }

    private fun getNeuralHandler(instance: MainActivity): NeuralHandler{
        // Load ANN weights and input ranges
        // TODO: Can we move loading the weights to the NeuralHandler class?
        var ins: InputStream = resources.openRawResource(R.raw.input_to_hidden_weights_and_biases)
        val inputToHiddenWeightsAndBiasesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        ins = resources.openRawResource(R.raw.hidden_to_output_weights_and_biases)
        val hiddenToOutputWeightsAndBiasesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        ins = resources.openRawResource(R.raw.input_ranges)
        val inputRangesString = ins.bufferedReader().use { it.readText() }
        ins.close()
        return NeuralHandler(
            "andrew",
            inputToHiddenWeightsAndBiasesString,
            hiddenToOutputWeightsAndBiasesString,
            inputRangesString,
            numWindowsBatched,applicationContext,instance)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Delta", "Main Destroyed")
        fRaw.close()
        sensorManager.unregisterListener(this)
    }

    @Composable
    fun WearApp(uiState: MainUiState){
        DeltaTheme {
            val listState = rememberScalingLazyListState()
            Scaffold(
                timeText = {
                    TimeText(modifier = Modifier.scrollAway(listState))
                },
                positionIndicator = {
                    PositionIndicator(
                        scalingLazyListState = listState
                    )
                }
            ) {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    autoCentering = AutoCenteringParams(itemIndex = 0),
                    state = listState
                ) {
                    item { WelcomeText() }
                    item { SmokeStatisticsCard(
                        numberOfCigs = uiState.numberOfCigs,
                        numberOfPuffs = uiState.numberOfPuffs)}
                    item { ReportMissedCigChip(
                        chipText = "Report missed cig",
                        onChipClick = {
                            mViewModel.setShowConfirmReportFalseNegativeDialog(it)
                        })
                    }
                    item {
                        if(uiState.isSmoking){
                            CompactCallbackChip(
                                chipText="Finish cig",
                                chipColor = "#827978",
                                onChipClick = {
                                    mViewModel.setShowConfirmDoneSmokingDialog(true)
                                })
                        } else {
                            CompactCallbackChip(
                                chipText="Smoke cig",
                                chipColor = "#b52914",
                                onChipClick = {
                                    mViewModel.setShowConfirmSmokingDialog(true)
                                })
                        }
                    }
                    item {
                        sensorCoordinates(
                            sensorX = uiState.sensorX,
                            sensorY = uiState.sensorY,
                            sensorZ = uiState.sensorZ
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun CompactCallbackChip(chipText:String,chipColor:String,onChipClick: (Boolean) -> Unit){
            CompactChip(
                onClick = { onChipClick(true) },
                enabled = true,
                // CompactChip label should be no more than 1 line of text
                label = {
                    Text(chipText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lungs),
                        contentDescription = "airplane",
                        modifier = Modifier.size(ChipDefaults.SmallIconSize),
                    )
                },
                colors = ChipDefaults.chipColors(backgroundColor = Color(parseColor(chipColor)))
            )
    }
    @Composable
    fun ConfirmDoneSmokingDialog(showDialog: Boolean,onDialogResponse: (Boolean) -> Unit){
        // TODO add timer to dialog
        val scrollState = rememberScalingLazyListState()
        Dialog(
            showDialog = showDialog,
            onDismissRequest = { },     // do not allow dismissal without responding
            scrollState = scrollState
        ) {
            Alert(
                scrollState = scrollState,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                contentPadding =
                PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lungs),
                        contentDescription = "lungs",
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                title = { Text(text = "Are you done smoking?", textAlign = TextAlign.Center) },
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Chip(
                            label = { Text("Yes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            onClick = { onDialogResponse(true) },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.width(60.dp)
                        )

                        Chip(
                            label = { Text("No", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            onClick = { onDialogResponse(false) },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.width(60.dp)
                        )

                    }
                }
            }
        }
    }
    @Composable
    fun ConfirmSmokeACigDialog(showDialog: Boolean,onDialogResponse: (Boolean) -> Unit){
        // TODO add timer to dialog
        val scrollState = rememberScalingLazyListState()
         Dialog(
                showDialog = showDialog,
                onDismissRequest = { },     // do not allow dismissal without responding
                scrollState = scrollState
            ) {
                Alert(
                    scrollState = scrollState,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                    contentPadding =
                    PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.lungs),
                            contentDescription = "lungs",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    title = { Text(text = "Do you want to record a smoking session?", textAlign = TextAlign.Center) },
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Chip(
                                    label = { Text("Yes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    onClick = { onDialogResponse(true) },
                                    colors = ChipDefaults.primaryChipColors(),
                                    modifier = Modifier.width(60.dp)
                                )

                                Chip(
                                    label = { Text("No", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    onClick = { onDialogResponse(false) },
                                    colors = ChipDefaults.secondaryChipColors(),
                                    modifier = Modifier.width(60.dp)
                                )

                        }
                    }
                }
            }
    }
    @Composable
    fun SmokeStatisticsCard(numberOfCigs: Int, numberOfPuffs: Int){
        Card(
            onClick = { /* ... */ }
        ) {
            Text("Looks like you've only smoked $numberOfCigs cigs and $numberOfPuffs puffs!")
        }
    }
    @Composable
    fun ReportMissedCigChip(chipText:String,onChipClick: (Boolean) -> Unit){
        CompactChip(
            onClick = { onChipClick(true) },
            enabled = true,
            // CompactChip label should be no more than 1 line of text
            label = {
                Text(chipText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.lungs),
                    contentDescription = "airplane",
                    modifier = Modifier.size(ChipDefaults.SmallIconSize),
                )
            },
        )
    }
    @Composable
    fun ReportMissedCigDialog(showDialog: Boolean,onDialogResponse: (Boolean) -> Unit){
        // TODO add timer to dialog
        val scrollState = rememberScalingLazyListState()
        Dialog(
            showDialog = showDialog,
            onDismissRequest = { onDialogResponse(false) },     // do not allow dismissal without responding
            scrollState = scrollState
        ) {
            Alert(
                scrollState = scrollState,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                contentPadding =
                PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lungs),
                        contentDescription = "lungs",
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                title = { Text(text = "Do you want to report a missed cig?", textAlign = TextAlign.Center) },
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Chip(
                            label = { Text("Yes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            onClick = { onDialogResponse(true) },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.width(60.dp)
                        )

                        Chip(
                            label = { Text("No", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                            onClick = { onDialogResponse(false) },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.width(60.dp)
                        )

                    }
                }
            }
        }
    }
    @Composable
    fun WelcomeText(modifier: Modifier = Modifier){
        Text(
            modifier = modifier,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = stringResource(id = R.string.title_text)
        )
    }

    @Composable
    fun sensorCoordinates(modifier: Modifier = Modifier, sensorX: String, sensorY: String, sensorZ: String){
        Card(
            onClick = { /* ... */ }
        ) {
            Text(sensorX)
            Text(sensorY)
            Text(sensorZ)
        }
    }
}