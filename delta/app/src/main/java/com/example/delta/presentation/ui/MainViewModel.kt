package com.example.delta.presentation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var isSmokingState by mutableStateOf(false)
    var xIsLessThanNegFour by mutableStateOf("No")
}