package com.example.delta.presentation

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import com.example.delta.presentation.Matrix.Companion.logSigmoid
import com.example.delta.presentation.Matrix.Companion.minMaxNorm
import com.example.delta.presentation.Matrix.Companion.tanSigmoid
import java.io.FileOutputStream


class NeuralHandler (name: String,
                     inputToHiddenWeightsAndBiasesString: String,
                     hiddenToOutputWeightsAndBiasesString: String,
                     inputRangesString:String,
                     private var numWindows: Int, applicationContext: Context,instance: MainActivity){
    val mName = name.uppercase()

    private var inputToHiddenWeightsAndBiases: Matrix
    private var hiddenToOutputWeightsAndBiases: Matrix
    private var inputRanges: Matrix
    private val windowSize = 100
    private var state = 0
    private var sessionState = 0
    private var currentPuffLength = 0
    private var currentInterPuffIntervalLength = 0
    private val applicationContext = applicationContext
    private val instance: MainActivity = instance

    init{
        Log.d("0010","Initializing Neural Handler...")
        inputToHiddenWeightsAndBiases = Matrix(inputToHiddenWeightsAndBiasesString)
        hiddenToOutputWeightsAndBiases = Matrix(hiddenToOutputWeightsAndBiasesString)
        inputRanges = Matrix(inputRangesString)
        if (numWindows < 1) {
            throw IllegalArgumentException("Number of Windows Batched must be 1 or greater")
        }
    }

    fun processBatch(extrasBuffer: MutableList<MutableList<String>>,
                     xBuffer: MutableList<MutableList<Double>>,
                     yBuffer: MutableList<MutableList<Double>>,
                     zBuffer: MutableList<MutableList<Double>>,
                     fRaw: FileOutputStream) {
        /*
            extrasBuffer: 3x200 timestamps and activity with each row:
                            [SensorEvent timestamp (ns), Calendar timestamp (ms), current activity]
            xBuffer:    1x200 x-axis accelerometer data values
            yBuffer:    1x200 y-axis accelerometer data values
            zBuffer:    1x200 z-axis accelerometer data values
            fRaw:       File output stream to write accelerometer raw data

            This function calls forwardPropagate for each of the windows
            (for 100 windows: [0-100...99-199] in the input matrix and then writes the data points
            and ANN outputs to a file
        */

        Log.v("0004","x: ${xBuffer.size}     y: ${yBuffer.size}    z: ${zBuffer.size}, extras: ${extrasBuffer.size}")
        var smokingOutput: Double

        // Run ANN on windows
        var i = 0
        while(i < numWindows){
            smokingOutput = forwardPropagate(
                Matrix((xBuffer.slice(i until i+windowSize)+
                        yBuffer.slice(i until i+windowSize)+
                        zBuffer.slice(i until i+windowSize)).toMutableList()))
            if (smokingOutput >= 0.85){
                smokingOutput = 1.0
            }
            else{
                smokingOutput = 0.0
            }
            // puff counter
            if (state == 0 && smokingOutput == 0.0){
                // no action
            } else if (state == 0 && smokingOutput == 1.0){
                // starting validating puff length
                state = 1
                currentPuffLength ++
            } else if (state == 1 && smokingOutput == 1.0){
                // continuing not yet valid length puff
                currentPuffLength ++
                if (currentPuffLength > 14) {
                    // valid puff length!
                    state = 2
                }
            } else if (state == 1 && smokingOutput == 0.0){
                // never was a puff, begin validating end
                state = 3
                currentInterPuffIntervalLength ++
            } else if (state == 2 && smokingOutput == 1.0){
                // continuing already valid puff
                currentPuffLength ++
            } else if (state == 2 && smokingOutput == 0.0){
                // ending already valid puff length
                state = 4 // begin validating inter puff interval
                currentInterPuffIntervalLength ++
            } else if (state == 3 && smokingOutput == 0.0) {
                currentInterPuffIntervalLength ++
                if (currentInterPuffIntervalLength > 49){
                    // valid interpuff
                    state = 0
                    currentPuffLength = 0
                    currentInterPuffIntervalLength = 0
                }
            } else if (state == 3 && smokingOutput == 1.0){
                // was validating interpuff for puff that wasn't valid
                currentPuffLength ++
                currentInterPuffIntervalLength = 0
                if (currentPuffLength > 14) {
                    // valid puff length!
                    state = 2
                }
                state = 1
            } else if (state == 4 && smokingOutput == 0.0) {
                currentInterPuffIntervalLength ++
                if (currentInterPuffIntervalLength > 49){
                    // valid interpuff for valid puff
                    state = 0
                    currentPuffLength = 0
                    currentInterPuffIntervalLength = 0
//                    onPuffDetected()
                }
            } else if (state == 4 && smokingOutput == 1.0){
                // back into puff for already valid puff
                currentInterPuffIntervalLength = 0
                currentPuffLength ++
                state = 2
            }
//            Log.d("0000","state $state")
            fRaw.write((extrasBuffer[i][0]+","+
                    xBuffer[i][0]+","+
                    yBuffer[i][0]+","+
                    zBuffer[i][0]+","+
                    extrasBuffer[i][1]+","+
                    extrasBuffer[i][2]+","+
                    smokingOutput.toString()+","+
                    state.toString()+"\n").toByteArray())
            i++
        }
    }
    private fun onPuffDetected(){
        instance.viewModel.iterateNumberOfPuffs()
        Log.d("0000","puff detected")
        if(!instance.isSmoking){
            Log.d("0000","user is not already smoking")
            // if not in session and puff detected, start internal timer
            if(sessionState == 0) {
                // seen 1 puff
                sessionState = 1

                timer.start()
            } else if (sessionState == 1){
                // seen 2 puffs
                sessionState = 2
            } else if (sessionState == 2){
                sessionState = 3
                timer.transferTimerToUiThread()
            }
        } else {
            timer.onFinish()
            sessionState = 0
        }
    }
    private val timer = object : CountDownTimer(100000, 1000) {
        var progress: Long = 0
        var progress_float = 0.0f
        override fun onTick(millisUntilFinished: Long) {
            progress += 1000
            progress_float += 0.01f
            Log.d("0000", progress.toString())

        }
        fun transferTimerToUiThread(){
            Log.d("0000", "Stopping neural handler timer, passing off to UI")
            Log.d("0000","Current progress $progress")
            instance.startSmoking(100000-progress,progress_float)
            cancel()
        }
        override fun onFinish() {
            cancel()
        }
    }
    private fun forwardPropagate(input: Matrix): Double {
        /*
            input : three-axis accelerometer values from smartwatch sampled at 20 Hz for 5 seconds.
                    i.e. the input is (1x300) where the first 100 values are x accelerometer
                    readings, the second 100 values are y accelerometer readings, and the last
                    are z accelerometer readings. The vector sort of looks like this:
                            [x,x,x,x,...,x,y,y,y,...,y,z,z,z,...,z].
                    If anyone asks where this info is found, see (Cole et al. 2017) at
                    https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5745355/.
         */
        var normedInput = minMaxNorm(input)
        normedInput.addOneToFront()
        var hiddenOutput = tanSigmoid(inputToHiddenWeightsAndBiases * normedInput)
        hiddenOutput.addOneToFront()
        return logSigmoid(hiddenToOutputWeightsAndBiases * hiddenOutput)[0][0]
    }
}