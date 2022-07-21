package com.example.deltamobile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.welcome__item_custom_row.view.*

// adapter is responsible for pulling data into the recyclerview layouts
class ItemAdapter(private val context:Context, private val itemList:List<UpdateItem>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // called as soon as view holder is visible / created in app
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.welcome__item_custom_row,parent,false)
        return ViewHolder(itemView)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = itemList.get(position)
        // set the text of the item to the text of the item
        holder.tvHeaderText.text = item.text1
        holder.tvSubText.text = item.text2
        holder.imgView.setImageResource(item.imageResource)

        // Updating the background color according to the odd/even positions in list.
        if (position % 2 == 0) {
            holder.cardViewItem.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_grey
                )
            )
        } else {
            holder.cardViewItem.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * Is basically one row in the viewholder
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        // Holds the TextView that will add each item to
        val tvHeaderText= itemView.tvHeaderText
        val tvSubText = itemView.tvSubText
        val cardViewItem = itemView.card_view_item
        val imgView = itemView.imgItem

        init {
            itemView.setOnClickListener(this,)
        }
        override fun onClick(v:View?){
            // adapter position is a property of recyclerview class
            val position = adapterPosition
            // check if position still valid
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener{
        // forward the position of the item clicked
        fun onItemClick(position:Int)
    }
}