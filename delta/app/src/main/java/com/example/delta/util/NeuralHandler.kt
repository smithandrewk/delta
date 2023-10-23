package com.example.delta.util

import com.example.delta.presentation.ui.MainViewModel
import com.example.delta.submodules.util.Matrix
import com.example.delta.submodules.util.Matrix.Companion.logSigmoid
import com.example.delta.submodules.util.Matrix.Companion.tanSigmoid


class NeuralHandler(
    inputToHiddenWeightsAndBiasesString: String,
    hiddenToOutputWeightsAndBiasesString: String,
    inputRangesString: String,
    private var numWindows: Int,
    filesHandler: FilesHandler,
    mViewModel: MainViewModel
){

    private var inputToHiddenWeightsAndBiases: Matrix
    private var hiddenToOutputWeightsAndBiases: Matrix
    private var inputRanges: Matrix
    private val windowSize = 100
    private var state = 0
    private var currentPuffLength = 0
    private var currentInterPuffIntervalLength = 0
    private val filesHandler = filesHandler
    private val mViewModel = mViewModel


    init{
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
                     zBuffer: MutableList<MutableList<Double>>) {
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

        var smokingOutput: Double
        var rawSmokingOutput: Double

        // Run ANN on windows
        var i = 0
        while(i < numWindows){
            rawSmokingOutput = forwardPropagate(
                Matrix((xBuffer.slice(i until i+windowSize)+
                        yBuffer.slice(i until i+windowSize)+
                        zBuffer.slice(i until i+windowSize)).toMutableList())
            )

            smokingOutput = if (rawSmokingOutput >= 0.85){
                1.0
            } else{
                0.0
            }
            // puff counter
            if (state == 0 && smokingOutput == 0.0){
                // no action
                state = 0
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
                } else {
                    state = 1
                }
            } else if (state == 4 && smokingOutput == 0.0) {
                currentInterPuffIntervalLength ++
                if (currentInterPuffIntervalLength > 49){
                    // valid inter-puff for valid puff
                    state = 0
                    currentPuffLength = 0
                    currentInterPuffIntervalLength = 0
                    mViewModel.onPuffDetected()
                }
            } else if (state == 4 && smokingOutput == 1.0){
                // back into puff for already valid puff
                currentInterPuffIntervalLength = 0
                currentPuffLength ++
                state = 2
            }
            filesHandler.writeToRawFile(eventTimeStamp = extrasBuffer[i][0],
                                        x = xBuffer[i][0],
                                        y = yBuffer[i][0],
                                        z = zBuffer[i][0],
                                        rawSmokingOutput = rawSmokingOutput,
                                        expertStateMachineState = state)
            i++
        }
    }
    private fun minMaxNorm(input: Matrix): Matrix {
        val output = input.copy()
        for (i in 0 until input.getRowSize()) {
            output[i][0] = ((2*((input[i][0] - inputRanges[i][0]) / (inputRanges[i][1] - inputRanges[i][0])))-1)
        }
        return output
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