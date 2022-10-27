package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileOutputStream
import java.util.*


class EndActivityButton : FragmentActivity() {
    private var cTimer: CountDownTimer? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var activityDetectedReceiver: ActionDetectedReceiver

    private lateinit var dataFolderName: String
    private lateinit var fPuffs: FileOutputStream
    private lateinit var puffsFilename: String
    private lateinit var vibrator: Vibrator
    private var dialogSendTime: Long = 0
    private var isADialogActive: Boolean = false
    private lateinit var currentDialog: PuffDetectedDialog

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_button)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

        createFile(intent)

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
                vibrator.vibrate(100)
                Log.i("0002", "Smoking Detected")
                dialogSendTime = Calendar.getInstance().timeInMillis

                if(isADialogActive){
                    currentDialog.dismiss()
                    isADialogActive = false
                }
                PuffDetectedDialog().show(this@EndActivityButton.supportFragmentManager, "PuffDetectedDialogueTransactionTag")
                isADialogActive = true
                // TODO timer to dismiss dialog if no response (response = 0)
            }
        }
    }
    public fun onPositiveDialog() {
        Log.i("0002", "Smoking Confirmed")
        // Write result to file
        isADialogActive = false
        fPuffs.write("${dialogSendTime}, 1\n".toByteArray())
    }
    public fun onNegativeDialog() {
        Log.i("0002", "Smoking Rejected")
        isADialogActive = false
        fPuffs.write("${dialogSendTime}, -1\n".toByteArray())
    }
    private fun createFile(intent: Intent){
        dataFolderName = intent.getStringExtra("StartTime") as String
        puffsFilename = "puffs-$dataFolderName.csv"
        fPuffs = FileOutputStream(File(this.filesDir, "$dataFolderName/$puffsFilename"))
        fPuffs.write("real time,response\n".toByteArray())
    }
    override fun onDestroy() {
        super.onDestroy()
        fPuffs.close()
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