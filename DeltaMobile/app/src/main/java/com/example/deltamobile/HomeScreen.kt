package com.example.deltamobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/*
 This is the dashboard of the delta app
 */
class HomeScreen : AppCompatActivity() , ItemAdapter.OnItemClickListener{
    private val listUpdates = getItemsList();
    // init the adapter class
    // where you pass data in
    // TO DO: PASS DATA IN DEPENDING ON WHAT WE WOULD LIKE
    //
    private val itemAdapter = ItemAdapter(this,listUpdates,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homescreen__base)

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
            openDashboard()
        }
        rvItem.adapter= this.itemAdapter

        // hide the action bar
        //
        supportActionBar?.hide()
    }

    // function to get items for recycler view
    //
    private fun getItemsList():ArrayList<UpdateItem>{
        val list = ArrayList<UpdateItem>()
        for(i in 0 until 12){
            val drawable = when(i%3){
                0 -> R.drawable.ic_baseline_accessibility_24
                1 -> R.drawable.ic_baseline_bluetooth_24
                else -> R.drawable.ic_baseline_cloud_upload_24
            }
            val item:UpdateItem = UpdateItem(drawable,"Item $i","Description $i")
            list.add(item)
        }
        return list
    }
    // function to open the dashboard
    //
    fun openDashboard(){
        val intentOpenHomeScreen: Intent = Intent(this,Dashboard::class.java)
        startActivity(intentOpenHomeScreen)
    }
    // implement on item click listener since interface
    override fun onItemClick(position: Int) {
        // remove the item
        //
        this.listUpdates.removeAt(position)
        this.itemAdapter.notifyItemRemoved(position)
    }
}