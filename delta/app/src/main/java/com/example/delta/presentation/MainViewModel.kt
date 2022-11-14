package com.example.delta.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    var showConfirmReportFalseNegativeDialog: Boolean = false,
    var showConfirmSmokingDialog: Boolean = false,
    var showConfirmDoneSmokingDialog: Boolean = false,
    val isSmoking: Boolean = false,
    val numberOfCigs: Int = 0,
    val numberOfPuffs: Int = 0,
    val progress: Float = 0.0f,
    val sensorX: String = "No Data",
    val sensorY: String = "No Data",
    val sensorZ: String = "No Data"
)

class MainViewModel : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
//    var sensorData by mutableStateOf("No Data")

    fun updateSensorData(sensorX: String, sensorY: String, sensorZ: String){
//        sensorData = value
        _uiState.update { currentState ->
            currentState.copy(
                sensorX = sensorX,
                sensorY = sensorY,
                sensorZ = sensorZ
            )
        }

    }

    // Handle business logic
    fun setShowConfirmReportFalseNegativeDialog(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showConfirmReportFalseNegativeDialog = value
            )
        }
    }
    fun setShowConfirmSmokingDialog(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showConfirmSmokingDialog = value
            )
        }
    }
    fun setShowConfirmDoneSmokingDialog(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showConfirmDoneSmokingDialog = value
            )
        }
    }
    fun setIsSmoking(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isSmoking = value
            )
        }
    }
    fun iterateNumberOfCigs(){
        _uiState.update { currentState ->
            currentState.copy(
                numberOfCigs = currentState.numberOfCigs + 1
            )
        }
    }
    fun iterateNumberOfPuffs(){
        _uiState.update { currentState ->
            currentState.copy(
                numberOfPuffs = currentState.numberOfPuffs + 1
            )
        }
    }
    fun setProgressZero(){
        _uiState.update { currentState ->
            currentState.copy(
                progress = 0.0f
            )
        }
    }
    fun setProgress(value: Float){
        _uiState.update { currentState ->
            currentState.copy(
                progress = value
            )
        }
    }
    fun iterateProgressByFloat(value: Float){
        _uiState.update { currentState ->
            currentState.copy(
                progress = currentState.progress + value
            )
        }
    }
}