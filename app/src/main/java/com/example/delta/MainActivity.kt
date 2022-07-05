package com.example.delta

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import com.example.delta.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toggle: ToggleButton = findViewById(R.id.activityToggleButton)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("PRINT", "on")
            } else {
                Log.d("PRINT", "off")
            }
        }
    }
}