package com.example.deltamobile.DashboardTabNav

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deltamobile.DashboardTabNav.HomeFrag.*
import com.example.deltamobile.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import kotlinx.android.synthetic.main.dashboard__frag_home.*
import kotlinx.android.synthetic.main.dashboard__frag_home__card_cell.view.*
import kotlin.math.sin

// home screen on dashboard
//
class HomeFragment : Fragment() ,CardAdapter.OnMyCardClickListener{
    private var listCards: ArrayList<MyCard> = ArrayList<MyCard>()
    private lateinit var cardAdapter: CardAdapter;

    /*
    TO FIX FRAGMENT NOT ATTACHED ERROR
    https://stackoverflow.com/questions/68458684/fragment-is-not-attached-to-a-context
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // create card objects
        this.listCards = getCardsList();

        // create card adapter
        //
        cardAdapter = CardAdapter(this.requireContext(),listCards,this)

        // create recycler view
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
        // return a list of cards to populate the cards
        //
        val list = ArrayList<MyCard>()
        for(i in 0 until 12){
            val drawable = getCardIcon(i);
            val card= MyCard(drawable,"Card $i","Date $i","Description $i",getCardLineChart(i));
            list.add(card)
        }
        return list;
    }
    private fun getCardLineChart(index:Int):LineChart{
        Utils.init(context)
        var x = arrayListOf<Float>()
        var y = arrayListOf<Float>()
        if(index%3 == 0){
            for(i in 0..9){
                x.add((i.toFloat()+ 0F))
                y.add(x.get(i)*x.get(i))
            }
        }else if(index%3 ==1){
            for(i in 0..9){
                x.add(i.toFloat()+0F)
                y.add(x.get(i))
            }
        }else{
            for(i in 0..9){
                x.add(i.toFloat())
                y.add(sin(x.get(i)))
            }
        }
        // return line chart for card
        // atm hardcoded for same values
        var entryList = mutableListOf<Entry>()
        for(i in 0 until x.size){
            entryList.add(
                Entry(x.get(i),y.get(i))
            )
        }
        // LineDataSet's list
        val lineDataSets = mutableListOf<ILineDataSet>()
        // put data in DataSet
        val lineDataSet = LineDataSet(entryList,"function of y and x")
        // format colors
        lineDataSet.color = Color.BLUE
        lineDataSets.add(lineDataSet)
        val lineData = LineData(lineDataSets)
        var lineChart = LineChart(this.context)
        lineChart.data = lineData
        lineChart.xAxis.apply{
            isEnabled = true
            textColor = Color.BLACK
        }
        return lineChart;
    }

    private fun getCardIcon(i:Int):Int{
        // get the icon shown on card
        val drawable = when(i%3){
            0->R.drawable.ic_baseline_accessibility_24
            1->R.drawable.ic_baseline_cloud_upload_24
            else ->R.drawable.ic_baseline_bluetooth_24
        }
        return drawable
    }

    // add onclicks to the buttons
    override fun onMyCardClick(position: Int,action:String) {
        // sanity check
//        Toast.makeText(this.context,"hello", Toast.LENGTH_SHORT).show()
        // get card that was clicked
        //
        val myCard = listCards[position]

        // check out card action
        //
        when(action){
            // see more about a card
            // this actually goes into a new view
            //
            action__DETAIL_CARD->{
                this.goToDetail(myCard)
            }
            // delete a card
            //
            action__DELETE_CARD->{
                this.deleteCard(position)
            }
        }
    }
    private fun deleteCard(position:Int){
        // TODO:
        // Cards themselves should represent some item from a database.
        // This item should be marked as checked when you delete.
        // deletes card and updates adapter
        this.listCards.removeAt(position)
        rvRecycler.adapter?.notifyItemRemoved(position)
    }
    private fun goToDetail(myCard:MyCard){
        // enter detail card activity
        //
        val intent = Intent(this.context,DashboardHomeFragDetailCardActivity::class.java)
        // put intent values in
        // NOTE: In reality we should make use of some db and pull from there.
        //
        intent.putExtra(intent__MYCARD_TITLE,myCard.cardTitle)
        intent.putExtra(intent__MYCARD_DESCRIPTION,myCard.cardDescription)
        intent.putExtra(intent__MYCARD_DATE,myCard.cardDate)
        intent.putExtra(intent__MYCARD_IMG_RES,myCard.imgResource)

        startActivity(intent)
    }
}