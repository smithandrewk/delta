package com.example.delta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class ChooseActivity : Activity() {
    private lateinit var chosenActivity: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        // get chosen activity from user
        chosenActivity = "Eating"   // temporary

        findViewById<Button>(R.id.button).setOnClickListener{
            var returnIntent = Intent()
            returnIntent.putExtra("chosenActivity", chosenActivity)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}