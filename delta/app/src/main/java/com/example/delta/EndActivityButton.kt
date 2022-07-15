package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.wear.widget.CurvedTextView


class EndActivityButton : Activity() {
    private var cTimer: CountDownTimer? = null
    private lateinit var progressBar: ProgressBar

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_button)

        // stuff to display file and sampling frequency
        val samplingRateHertz = intent.getStringExtra("SamplingRateKey")
        val filename = intent.getStringExtra("FilenameKey")
        val xmlSamplingFrequency: CurvedTextView = findViewById(R.id.samplingFrequency)
        val xmlFilename: CurvedTextView = findViewById(R.id.filename)
        xmlSamplingFrequency.text = "$samplingRateHertz Hz"
        xmlFilename.text = filename

        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.endActivityButton).setOnTouchListener { _, event -> //when button is pressed
            if (event.action == MotionEvent.ACTION_DOWN) {
                //show the progressBar
                progressBar.visibility = View.VISIBLE
                startTimer()
            } else if (event.action == MotionEvent.ACTION_UP) {
                progressBar.progress = 0
                progressBar.visibility = View.INVISIBLE
                cancelTimer()
            }
            true
        }

    }

    //start timer function
    private fun startTimer() {
        cTimer = object : CountDownTimer(2000, 20) {
            override fun onTick(millisUntilFinished: Long) {
                progressBar.progress = progressBar.progress + 1
            }

            override fun onFinish() {
                progressBar.progress = 0
                progressBar.visibility = View.INVISIBLE
                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
        (cTimer as CountDownTimer).start()
    }

    //cancel timer
    private fun cancelTimer() {
        if (cTimer != null) cTimer!!.cancel()
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