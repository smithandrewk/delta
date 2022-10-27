package com.example.delta

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import java.util.*

class PuffDetectedDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->

                when (which) {
                    // on below line we are setting a click listener
                    // for our positive button
                    DialogInterface.BUTTON_POSITIVE -> {
                        // on below line we are displaying a toast message.
                        Log.i("0005", "Smoking Confirmed")
                        (activity as EndActivityButton).onPositiveDialog()
                    }

                    // on below line we are setting click listener
                    // for our negative button.
                    DialogInterface.BUTTON_NEGATIVE -> {
                        // on below line we are dismissing our dialog box.
                        Log.i("0005", "Smoking Rejected")
                        (activity as EndActivityButton).onNegativeDialog()
                        dialog.dismiss()
                    }
                }
            }
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

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
            .setView(R.layout.dialog_layout)
        return builder.create()
    }
}