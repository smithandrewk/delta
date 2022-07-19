package com.example.deltamobile.DashboardTabNav

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltamobile.DashboardTabNav.HomeFrag.*
import com.example.deltamobile.R
import kotlinx.android.synthetic.main.dashboard__frag_home.*

// home screen on dashboard
//
class HomeFragment : Fragment() ,CardAdapter.OnMyCardClickListener{
    private val listCards = getCardsList();

    private lateinit var cardAdapter: CardAdapter;

    /*
    TO FIX FRAGMENT NOT ATTACHED ERROR
    https://stackoverflow.com/questions/68458684/fragment-is-not-attached-to-a-context
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardAdapter = CardAdapter(this.requireContext(),listCards,this)

        val rvRecycler:RecyclerView = this.rvRecycler
        val SPAN_COUNT = 2
        rvRecycler.layoutManager = GridLayoutManager(this.requireContext(),SPAN_COUNT)
        rvRecycler.adapter = this.cardAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dashboard__frag_home, container, false)
    }

    private fun getCardsList():ArrayList<MyCard>{
        val list = ArrayList<MyCard>()
        for(i in 0 until 12){
            val drawable = when(i%3){
                0->R.drawable.ic_baseline_accessibility_24
                1->R.drawable.ic_baseline_cloud_upload_24
                else ->R.drawable.ic_baseline_bluetooth_24
            }
            val card= MyCard(drawable,"Card $i","Date $i","Description $i")
            list.add(card)
        }
        return list;
    }

    // add onclicks to the buttons
    // for sending data, see Safe Args:
    //https://stackoverflow.com/questions/62308730/passing-data-from-activity-to-fragment-using-safe-args
    //https://www.section.io/engineering-education/safe-args-in-android/
    override fun onMyCardClick(position: Int) {
        // sanity check
//        Toast.makeText(this.context,"hello", Toast.LENGTH_SHORT).show()
        // get card that was clicked
        //
        val myCard = listCards[position]
        // enter detail card activity
        //
        val intent = Intent(this.context,DashboardHomeFragDetailCardActivity::class.java)
        intent.putExtra(intent__MYCARD_TITLE,myCard.cardTitle)
        intent.putExtra(intent__MYCARD_DESCRIPTION,myCard.cardDescription)
        intent.putExtra(intent__MYCARD_DATE,myCard.cardDate)
        intent.putExtra(intent__MYCARD_IMG_RES,myCard.imgResource)

        startActivity(intent)
    }
}