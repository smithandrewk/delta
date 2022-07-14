package com.example.delta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import androidx.wear.widget.CurvedTextView


class EndActivityButton : Activity() {
    var cTimer: CountDownTimer? = null
//    var progressBarAnswer =
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
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

    }

//    //start timer function
//    fun startTimer() {
//        cTimer = object : CountDownTimer(2000, 200) {
//            override fun onTick(millisUntilFinished: Long) {
//                progressBarAnswer.setProgress(progressBarAnswer.getProgress() + 10)
//            }
//
//            override fun onFinish() {
//                progressBarAnswer.setProgress(0)
//                progressBarAnswer.setVisibility(View.INVISIBLE)
//                showAnswer()
//            }
//        }
//        cTimer.start()
//    }
//
//    //cancel timer
//    fun cancelTimer() {
//        if (cTimer != null) cTimer!!.cancel()
//    }
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