package com.example.delta

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button

class ChooseActivity : Activity() {
    private val activityOptions = mapOf(R.id.eatButton to "Eating",
                                        R.id.drinkButton to "Drinking",
                                        R.id.smokeButton to "Smoking")

    private lateinit var chosenActivity: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        // get chosen activity from user - create onClickListener for each button and send corresponding string
        activityOptions.forEach { (button, chosenActivity) ->
            findViewById<Button>(button).setOnClickListener {
                val returnIntent = Intent()
                returnIntent.putExtra("chosenActivity", chosenActivity)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }
}