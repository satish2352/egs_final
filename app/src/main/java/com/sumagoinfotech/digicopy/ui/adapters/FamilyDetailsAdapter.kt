package com.sumagoinfotech.digicopy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R

class FamilyDetailsAdapter : RecyclerView.Adapter<FamilyDetailsAdapter.ViewHolder>() {


    // Adding elements to the ArrayList
    val stringList = ArrayList<String>()

    init {
        // Adding elements to the ArrayList
        stringList.add("Apple")
        stringList.add("Banana")
        stringList.add("Orange")

    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewDelete = itemView.findViewById<ImageView>(R.id.imageViewDelete)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FamilyDetailsAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_family_details_2,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyDetailsAdapter.ViewHolder, position: Int) {

        holder.imageViewDelete.setOnClickListener {
            notifyItemRemoved(position)
            stringList.remove(stringList.get(position))
        }

    }

    override fun getItemCount(): Int {
        return stringList.size
    }
}