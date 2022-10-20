package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.wear.widget.CurvedTextView
import java.util.*


class EndActivityButton : Activity() {
    private var cTimer: CountDownTimer? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var activityDetectedReceiver: ActionDetectedReceiver

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_button)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.endActivityButton).setOnTouchListener { _, event -> //when button is pressed
            if (event.action == MotionEvent.ACTION_DOWN) {
                // show the progressBar
                progressBar.visibility = View.VISIBLE
                startTimer()
            } else if (event.action == MotionEvent.ACTION_UP) {
                progressBar.visibility = View.INVISIBLE
                cancelTimer()
            }
            true
        }

        activityDetectedReceiver = ActionDetectedReceiver()
        registerReceiver(activityDetectedReceiver,
            IntentFilter(getString(R.string.ACTION_DETECTED_BROADCAST_CODE))
        )

    }

    //start timer function
    private fun startTimer() {
        cTimer = object : CountDownTimer(1400, 20) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                progressBar.visibility = View.INVISIBLE
                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
        (cTimer as CountDownTimer).start()
    }

    // cancel timer
    private fun cancelTimer() {
        if (cTimer != null) cTimer!!.cancel()
    }

    private inner class ActionDetectedReceiver : BroadcastReceiver() {
        // Inner class to define the broadcast receiver
        // This Broadcast Receiver receives signals from MainActivity when user presses buttons
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == context.getString(R.string.ACTION_DETECTED_BROADCAST_CODE)) {
                Log.i("Action Detected Receiver", "Received")
                val dialogClickListener =
                    DialogInterface.OnClickListener { dialog, which ->

                        when (which) {
                            // on below line we are setting a click listener
                            // for our positive button
                            DialogInterface.BUTTON_POSITIVE -> {
                                // on below line we are displaying a toast message.
                                Toast.makeText(this@EndActivityButton, "Yes clicked", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            // on below line we are setting click listener
                            // for our negative button.
                            DialogInterface.BUTTON_NEGATIVE -> {
                                // on below line we are dismissing our dialog box.
                                dialog.dismiss()
                            }
                        }
                    }
                // on below line we are creating a builder variable for our alert dialog
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@EndActivityButton)

                // on below line we are setting message for our dialog box.
                builder.setMessage("Puff ?")
                    // on below line we are setting positive
                    // button and setting text to it.
                    .setPositiveButton("Yes", dialogClickListener)
                    // on below line we are setting negative button
                    // and setting text to it.
                    .setNegativeButton("No", dialogClickListener)
                    // on below line we are calling
                    // show to display our dialog.
                    .show()

                // TODO timer to dismiss dialog if no response
                // TODO write response to a file
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(activityDetectedReceiver)
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