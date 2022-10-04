package com.example.delta

import android.util.Log
import com.example.delta.Matrix.Companion.logSigmoid
import com.example.delta.Matrix.Companion.minMaxNorm
import com.example.delta.Matrix.Companion.tanSigmoid
import java.io.FileOutputStream
import java.util.*


class NeuralHandler (name: String,inputToHiddenWeightsAndBiasesString: String,hiddenToOutputWeightsAndBiasesString: String,inputRangesString:String){
    val mName = name.uppercase()

    private var inputToHiddenWeightsAndBiases: Matrix
    private var hiddenToOutputWeightsAndBiases: Matrix
    private var inputRanges: Matrix

    init{
        Log.d("0010","Initializing Neural Handler...")
        inputToHiddenWeightsAndBiases = Matrix(inputToHiddenWeightsAndBiasesString)
        hiddenToOutputWeightsAndBiases = Matrix(hiddenToOutputWeightsAndBiasesString)
        inputRanges = Matrix(inputRangesString)
    }

    fun processBatch(timestampBuffer: MutableList<MutableList<String>>,
                     xBuffer: MutableList<MutableList<Double>>,
                     yBuffer: MutableList<MutableList<Double>>,
                     zBuffer: MutableList<MutableList<Double>>,
                     fRaw: FileOutputStream) {
        /*
            timestampBuffer: 2x200 timestamps with each row:
                            [SensorEvent timestamp (ns), Calendar timestamp (ms)]
            xBuffer:    1x200 x-axis accelerometer data values
            yBuffer:    1x200 y-axis accelerometer data values
            zBuffer:    1x200 z-axis accelerometer data values
            fRaw:       File output stream to write accelerometer raw data

            This function calls forwardPropagate for each of the 100 windows in the input matrix and
            then writes the data points and ANN outputs to a file
        */
        val bufferSize = 200
        Log.i("0004","x: ${xBuffer.size}     y: ${yBuffer.size}    z: ${zBuffer.size}")
//        if (xBuffer.size != bufferSize || yBuffer.size != bufferSize || zBuffer.size != bufferSize){
//            throw java.lang.IllegalArgumentException("Buffer size should be 200")
//        }
//        // Run ANN on windows
        var outputs: MutableList<Double> = mutableListOf()  // list of outputs for each window

        var i = 0
        while(i < bufferSize){
            outputs[i] = forwardPropagate()
        }

        // Write to file
        for(i in 0 until bufferSize){
            Log.i("0004-${Calendar.getInstance().timeInMillis}","label: none    Time: ${timestampBuffer[i][0]}      TimeMs: ${timestampBuffer[i][1]}       x: ${xBuffer[i]}     y: ${yBuffer[i]}    z: ${zBuffer[i]}")
//            fRaw.write((timestampBuffer[0]+","+
//                        event.values[0].toString()+","+
//                        event.values[1].toString()+","+
//                        event.values[2].toString()+","+
//                        Calendar.getInstance().timeInMillis+","+
//                        currentActivity+","+output.toString()+"\n").toByteArray())
        }
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