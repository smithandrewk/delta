package com.example.deltamobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/*
This is the start display for Delta.
 */

class MainActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        supportActionBar?.hide()
    }
}