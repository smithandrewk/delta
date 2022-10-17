package com.example.delta

import android.util.Log
import com.example.delta.Matrix.Companion.logSigmoid
import com.example.delta.Matrix.Companion.minMaxNorm
import com.example.delta.Matrix.Companion.tanSigmoid
import java.io.FileOutputStream
import java.util.*


class NeuralHandler (name: String,
                     inputToHiddenWeightsAndBiasesString: String,
                     hiddenToOutputWeightsAndBiasesString: String,
                     inputRangesString:String,
                     private var numWindows: Int){
    val mName = name.uppercase()

    private var inputToHiddenWeightsAndBiases: Matrix
    private var hiddenToOutputWeightsAndBiases: Matrix
    private var inputRanges: Matrix
    private val windowSize = 100

    private var temp = true

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
                     fRaw: FileOutputStream) : MutableSet<String> {
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

        Log.i("0004","x: ${xBuffer.size}     y: ${yBuffer.size}    z: ${zBuffer.size}, extras: ${extrasBuffer.size}")
        val activitiesDetected: MutableSet<String> = mutableSetOf()
        var smokingOutput: Double

        // Run ANN on windows
        var i = 0
        while(i < numWindows){
            smokingOutput = forwardPropagate(
                Matrix((xBuffer.slice(i until i+windowSize)+
                        yBuffer.slice(i until i+windowSize)+
                        zBuffer.slice(i until i+windowSize)).toMutableList()))
            if (smokingOutput >= 0.85 || temp){
                smokingOutput = 1.0
                activitiesDetected.add("Smoking")
                temp = false
            }
            else{
                smokingOutput = 0.0
            }
//            output = if(output >= .85){
//                1.0
//            } else {
//                0.0
//            }

            fRaw.write((extrasBuffer[i][0]+","+
                        xBuffer[i][0]+","+
                        yBuffer[i][0]+","+
                        zBuffer[i][0]+","+
                        extrasBuffer[i][1]+","+
                        extrasBuffer[i][2]+","+
                        smokingOutput.toString()+"\n").toByteArray())
            i++
        }
        return activitiesDetected
    }
    fun forwardPropagate(input: Matrix): Double {
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