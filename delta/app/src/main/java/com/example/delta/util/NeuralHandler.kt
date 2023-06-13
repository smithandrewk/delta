package com.example.delta.util

import android.content.Context
import android.util.Log
import com.example.delta.presentation.ui.MainViewModel
import java.io.FileOutputStream
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.IOException


class NeuralHandler (
    private var assetName: String,
    private var numWindows: Int,
    private var applicationContext: Context,
    filesHandler: FilesHandler,
    mViewModel: MainViewModel) {


    private lateinit var module: Module
    private val windowSize = 100
    private var state = 0
    private var currentPuffLength = 0
    private var currentInterPuffIntervalLength = 0
    private val filesHandler = filesHandler
    private val mViewModel = mViewModel


    init{
        loadModule()

        if (numWindows < 1) {
            throw IllegalArgumentException("Number of Windows Batched must be 1 or greater")
        }
    }

    private fun loadModule() {
        // Loads pytorch model onto watch and into "module" variable
        var moduleFileAbsoluteFilePath: String = ""
        val moduleFile = File(applicationContext.filesDir, assetName)   // File on watch
        try {
            applicationContext.assets.open(assetName).use { `is` -> // Read from module file in "assets" dir
                FileOutputStream(moduleFile).use { os ->    // Write to file on watch
                    val buffer = ByteArray(4 * 1024)
                    while (true) {
                        val length = `is`.read(buffer)
                        if (length <= 0)
                            break
                        os.write(buffer, 0, length)     // Write module to file on watch
                    }
                    os.flush()
                    os.close()
                }
                moduleFileAbsoluteFilePath = moduleFile.absolutePath    // Path to file on watch
            }
        } catch (e: IOException) {
            Log.e("Main", "Error process asset $assetName to file path")
        }

        // Load module
        module = LiteModuleLoader.load(moduleFileAbsoluteFilePath)
    }

    fun processBatch(extrasBuffer: MutableList<MutableList<String>>,
                     xBuffer: MutableList<MutableList<Float>>,
                     yBuffer: MutableList<MutableList<Float>>,
                     zBuffer: MutableList<MutableList<Float>>) {
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
            rawSmokingOutput = forwardPropagate((
                xBuffer.slice(i until i+windowSize) +
                yBuffer.slice(i until i+windowSize) +
                zBuffer.slice(i until i+windowSize)
            ).flatten().toFloatArray())

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
                                        acc_x = xBuffer[i][0].toDouble(),
                                        acc_y = yBuffer[i][0].toDouble(),
                                        acc_z = zBuffer[i][0].toDouble(),
                                        smokingStateString = extrasBuffer[i][1],
                                        rawSmokingOutput = rawSmokingOutput,
                                        expertStateMachineState = state)
            i++
        }
    }

    private fun forwardPropagate(input: FloatArray): Double {
        /*
            input : three-axis accelerometer values from smartwatch sampled at 20 Hz for 5 seconds.
                    i.e. the input is (1x300) where the first 100 values are x accelerometer
                    readings, the second 100 values are y accelerometer readings, and the last
                    are z accelerometer readings. The vector sort of looks like this:
                            [x,x,x,x,...,x,y,y,y,...,y,z,z,z,...,z].
                    If anyone asks where this info is found, see (Cole et al. 2017) at
                    https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5745355/.
         */

        // forward propagate on Pytorch module
        val inputTensor = Tensor.fromBlob(input, longArrayOf(300))
        val outputTensor = module.forward(IValue.from(inputTensor))?.toTensor()
        Log.d("Main", "${outputTensor?.dataAsFloatArray?.get(0)}")
        return outputTensor?.dataAsFloatArray?.get(0)?.toDouble() ?: 0.0
    }
}