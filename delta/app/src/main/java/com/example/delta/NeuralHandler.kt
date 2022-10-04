package com.example.delta

import android.util.Log
import com.example.delta.Matrix.Companion.logSigmoid
import com.example.delta.Matrix.Companion.minMaxNorm
import com.example.delta.Matrix.Companion.tanSigmoid
import java.io.FileOutputStream


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

    fun processBatch(xBuffer: MutableList<MutableList<Double>>,
                     yBuffer: MutableList<MutableList<Double>>,
                     zBuffer: MutableList<MutableList<Double>>,
                     fRaw: FileOutputStream) {
        /*
            xBuffer:    1x200 x-axis accelerometer data values
            yBuffer:    1x200 y-axis accelerometer data values
            zBuffer:    1x200 z-axis accelerometer data values
            fRaw:       File output stream to write accelerometer raw data

            This function calls forwardPropagate for each of the 100 windows in the input matrix and
            then writes the data points and ANN outputs to a file
        */
        val bufferSize = 200
        if (xBuffer.size != bufferSize || yBuffer.size != bufferSize || zBuffer.size != bufferSize){
            throw java.lang.IllegalArgumentException("Buffer size should be 200")
        }
//        // Run ANN on windows
//        var outputs: MutableList<Double> = mutableListOf()  // list of outputs for each window
//        var i = 0
//        while(i < 99){
//            forwardPropagate()
//        }

        // Write to file
        for(i in 0..bufferSize){
            Log.i("0004","label: none    Time: ${}    x: ${event.values[0]}     y: ${event.values[1]}    z: ${event.values[2]}")
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