package com.example.deltamobile.DashboardTabNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltamobile.DashboardTabNav.HomeFrag.CardAdapter
import com.example.deltamobile.DashboardTabNav.HomeFrag.MyCard
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
        rvRecycler.layoutManager = GridLayoutManager(this.requireContext(),2)
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
                else ->R.drawable.ic_baseline_settings_24
            }
            val card:MyCard = MyCard(drawable,"Card $i","Description $i")
            list.add(card)
        }
        return list;
    }

    override fun onMyCardClick(position: Int) {
        // do something
    }
}