package com.example.deltamobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AbsListView
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recyclerviewapp.ItemAdapter

/*
This is the start display for Delta.
 */

class MainActivity: AppCompatActivity() ,ItemAdapter.OnItemClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_display)

        // get the recyclerview
        //
        val rvItem:RecyclerView = findViewById(R.id.rvUpdates)

        rvItem.layoutManager = LinearLayoutManager(this)

        // init the adapter class
        // where you pass data in
        // TO DO: PASS DATA IN DEPENDING ON WHAT WE WOULD LIKE
        //
        val itemAdapter:ItemAdapter = ItemAdapter(this,getItemsList(),this)


        // set layout manager that this recycler view will use

        // add an on click for button
        //
        var btnGetStarted = findViewById<Button>(R.id.btnGetStarted)

        // get started btn opens dashboard
        //
        btnGetStarted.setOnClickListener{
            openDashboard()
        }
        rvItem.adapter= itemAdapter
    }
    // function to get items for recycler view
    //
    private fun getItemsList():ArrayList<String>{
        val list = ArrayList<String>()
        for(i in 0..5){
            list.add("Item $i")
        }
        return list
    }
    // function to open the dashboard
    //
    fun openDashboard(){
        val intentOpenDashboard:Intent = Intent(this,Dashboard::class.java)
        startActivity(intentOpenDashboard)
    }
    // implement on item click listener since interface
    override fun onItemClick(position: Int) {
        Toast.makeText(this, "Item $position clicked", Toast.LENGTH_SHORT).show()
        // get reference to clicked item
    }
}