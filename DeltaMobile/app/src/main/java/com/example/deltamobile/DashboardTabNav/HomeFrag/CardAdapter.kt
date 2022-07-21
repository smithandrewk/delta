package com.example.deltamobile.DashboardTabNav.HomeFrag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.deltamobile.R
import kotlinx.android.synthetic.main.dashboard__frag_home__card_cell.view.*

class CardAdapter(private val context: Context, private val cardList:List<MyCard>,private val listener:OnMyCardClickListener)
    :RecyclerView.Adapter<CardAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardAdapter.ViewHolder {
        val myCardView = LayoutInflater.from(parent.context).inflate(R.layout.dashboard__frag_home__card_cell,parent,false)
        return ViewHolder(myCardView)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    inner class ViewHolder(myCardView:View)
        :RecyclerView.ViewHolder(myCardView){

            // get elements from card
            //
            val tvTitle = myCardView.tvTitle;
            val imgCover = myCardView.imgCover;
            val tvDescription = myCardView.tvDescription;
            val tvDate = myCardView.tvDate;

            init{
                // set on click to detail card
                myCardView.btnClicker.setOnClickListener{
                    this.onClick(this.itemView, action__DETAIL_CARD)
                }
                // set on click to remove card
                myCardView.imgRemove.setOnClickListener{
                    this.onClick(this.itemView, action__DELETE_CARD)
                }
            }
            // called when click element
            private fun onClick(v:View?,action:String){
                val position = adapterPosition
                // check if position still valid
                // only really relevant when remove card
                if(position != RecyclerView.NO_POSITION){
                    listener.onMyCardClick(position,action)
                }
            }

    }
    interface OnMyCardClickListener{
        fun onMyCardClick(position:Int,action:String)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = cardList[position]

        holder.tvTitle.text = card.cardTitle
        holder.tvDate.text = card.cardDate
        holder.tvDescription.text = card.cardDescription
        holder.imgCover.setImageResource(card.imgResource)

    }
}
