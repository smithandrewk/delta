package com.example.deltamobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/*
This is the start display for Delta.
 */

////
// Store all the names of the SharedPreferences variables.
// Nomenclature:
// sp[capitalized activity name with spaces as underscore]__[all caps name of variable with spaces as underscores]
//
val spMAIN_ACTIVITY__BOARDING_COMPLETE = "spMAIN_ACTIVITY__BOARDING_COMPLETE";

////
// Store the names of the share preferences
// Nomenclature:
// spname[capitalized activity name with spaces as underscore]__[all caps name of shared pref with spaces as underscores]
//
val spnameMAIN_ACTIVITY__ON_BOARDING = "spnameMAIN_ACTIVITYON__BOARDING";
val spnameMAIN_ACTIVITY__UPDATES_VIEWED = "spnameMAIN_ACTIVITY__UPDATE_RELEASED"

class MainActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        supportActionBar?.hide()
    }
}