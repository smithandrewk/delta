package com.example.delta

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.wear.widget.CurvedTextView

class EndActivityButton : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_button)

        // stuff to display file and sampling frequency
        val samplingRateHertz = intent.getStringExtra("SamplingRateKey")
        val filename = intent.getStringExtra("FilenameKey")
        var xmlSamplingFrequency: CurvedTextView = findViewById(R.id.samplingFrequency)
        var xmlFilename: CurvedTextView = findViewById(R.id.filename)
        xmlSamplingFrequency.text = "$samplingRateHertz Hz"
        xmlFilename.text = filename

        // when button is clicked, go back to main activity
        findViewById<Button>(R.id.endActivityButton).setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.putExtra("EndActivityKey", "true")
            startActivity(mainIntent)
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        Log.i("0002", "DESTROYED")
    }
    override fun onStop() {
        super.onStop()
        Log.i("0002", "STOPPED")
    }
    override fun onPause() {
        super.onPause()
        Log.i("0002", "PAUSED")
    }
    override fun onStart() {
        super.onStart()
        Log.i("0002", "STARTED")
    }
    override fun onRestart() {
        super.onRestart()
        Log.i("0002", "RESTARTED")
    }
    override fun onResume() {
        super.onResume()
        Log.i("0002", "RESUMED")
    }
}