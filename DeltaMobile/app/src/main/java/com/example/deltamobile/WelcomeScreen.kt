package com.example.deltamobile

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

////
// WelcomeScreen
// This screen greets users immediately after boarding to notify of any updates.
// It is only shown if a boolean value in the SharedPreferences is set to True.
// Else this screen is not shown.
//
// TO DO:
// WELCOME SCREEN SHOULD BE FRAGMENT!!!
//
class WelcomeScreen : AppCompatActivity() , ItemAdapter.OnItemClickListener{
    private val listUpdates = getItemsList();
    // init the adapter class
    // where you pass data in
    // TO DO: PASS DATA IN DEPENDING ON WHAT WE WOULD LIKE
    //
    private val itemAdapter = ItemAdapter(this,listUpdates,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome__base)

        // get the recyclerview
        //
        val rvItem: RecyclerView = findViewById(R.id.rvUpdates)

        rvItem.layoutManager = LinearLayoutManager(this)

        // set layout manager that this recycler view will use

        // add an on click for button
        //
        var btnGetStarted = findViewById<Button>(R.id.btnGetStarted)

        // get started btn opens dashboard
        //
        btnGetStarted.setOnClickListener{

            // go to dashboard page
            openDashboard()
        }

        // attach adapter
        rvItem.adapter= this.itemAdapter

        // user has now viewed updates, so dont need to see this screen on reload
        //
        this.onUpdateViewingFinished();

        // hide the action bar
        //
        supportActionBar?.hide()
    }

    ////
    // getItemsList()
    // Input: void
    // Output: ArrayList<UpdateItem>
    // Utility: returns a list of update items to populate the update page
    //
    private fun getItemsList():ArrayList<UpdateItem>{
        val list = ArrayList<UpdateItem>()
        val intNumPictures = 3;
        val intNumUpdates = 12;

        for(i in 0 until intNumUpdates){
            val drawable = when(i%intNumPictures){
                0 -> R.drawable.ic_baseline_accessibility_24
                1 -> R.drawable.ic_baseline_bluetooth_24
                else -> R.drawable.ic_baseline_cloud_upload_24
            }
            val item = UpdateItem(drawable,"Update $i","Description $i")
            list.add(item)
        }
        return list
    }
    ////
    // store shared pref value that states that the user has completed viewing of updates
    //
    private fun onUpdateViewingFinished(){
        val sharedPref =getSharedPreferences(spnameMAIN_ACTIVITY__UPDATES_VIEWED, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(spnameMAIN_ACTIVITY__UPDATES_VIEWED,true)
        editor.apply()
    }
    ////
    // openDashboard()
    // Inputs: void
    // Outputs: void
    // Utility: starts an intent to open the dashboard from home.
    fun openDashboard(){
        val intentOpenHomeScreen: Intent = Intent(this,Dashboard::class.java)
        startActivity(intentOpenHomeScreen)
    }
    ////
    // onItemClick(position:Int)
    // Input: int
    // Output: void
    // Utility: when click an update item, make it do something.
    override fun onItemClick(position: Int) {
        // remove the item
        //
        this.listUpdates.removeAt(position)
        this.itemAdapter.notifyItemRemoved(position)
    }
}