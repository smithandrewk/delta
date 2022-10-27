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
    private lateinit var currentDialog: DialogInterface

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
                PuffDetectedDialog().show(this@EndActivityButton.supportFragmentManager, "tag")
//                dialogSendTime = Calendar.getInstance().timeInMillis
//                if(isADialogActive){
//                    currentDialog.dismiss()
//                    isADialogActive = false
//                }
//                Log.i("Action Detected Receiver", "Received")
//                val dialogClickListener =
//                    DialogInterface.OnClickListener { dialog, which ->
//                        currentDialog = dialog
//                        when (which) {
//                            // on below line we are setting a click listener
//                            // for our positive button                            DialogInterface.BUTTON_POSITIVE -> {
//                                // on below line we are displaying a toast message.
//                                Log.i("0002", "Smoking Confirmed")
//                                // Write result to file
//                                dialog.dismiss()
//                                isADialogActive = false
//                                fPuffs.write("${dialogSendTime}, 1\n".toByteArray())
//                            }
//
//                            // on below line we are setting click listener
//                            // for our negative button.
//                            DialogInterface.BUTTON_NEGATIVE -> {
//                                // on below line we are dismissing our dialog box.
//                                Log.i("0002", "Smoking Rejected")
//                                dialog.dismiss()
//                                isADialogActive = false
//                                fPuffs.write("${dialogSendTime}, -1\n".toByteArray())
//                            }
//                        }
//                    }
//                // on below line we are creating a builder variable for our alert dialog
//                val builder: AlertDialog.Builder = AlertDialog.Builder(this@EndActivityButton)
////                 on below line we are setting message for our dialog box.
//
//                builder.setMessage("Puff ?")
//                    // on below line we are setting positive
//                    // button and setting text to it.
//                    .setPositiveButton("Yes", dialogClickListener)
//                    // on below line we are setting negative button
//                    // and setting text to it.
//                    .setNegativeButton("No", dialogClickListener)
//                    // on below line we are calling
//                    // show to display our dialog.
//                    .show()
//                isADialogActive = true
                // TODO timer to dismiss dialog if no response (response = 0)
            }
        }
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