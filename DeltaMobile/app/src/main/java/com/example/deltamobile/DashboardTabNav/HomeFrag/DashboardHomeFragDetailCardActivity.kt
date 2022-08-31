package com.example.deltamobile.DashboardTabNav.HomeFrag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.deltamobile.Dashboard
import com.example.deltamobile.R
import kotlinx.android.synthetic.main.dashboard__frag_home__detail_card_activity.*

class DashboardHomeFragDetailCardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard__frag_home__detail_card_activity)

        // get values from intent
        //
        val receiveIntent = intent;
        tvTitle.text = receiveIntent.getStringExtra(intent__MYCARD_TITLE)
        tvDescription.text = receiveIntent.getStringExtra(intent__MYCARD_DESCRIPTION)
        tvDate.text = receiveIntent.getStringExtra(intent__MYCARD_DATE)
        imgCover.setImageResource(receiveIntent.getIntExtra(intent__MYCARD_IMG_RES,0))

        // on click go back to home frag
        //
        btnClicker.setOnClickListener{
            val intent = Intent(this,Dashboard::class.java)
            startActivity(intent)
        }

        // hide the action bar
        //
        supportActionBar?.hide()
    }
}