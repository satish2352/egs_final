package com.sumagoinfotech.digicopy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.model.FamilyDetails

class FamilyDetailsAdapter(var familyDetailsList: ArrayList<FamilyDetails>) : RecyclerView.Adapter<FamilyDetailsAdapter.ViewHolder>() {



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
            familyDetailsList.remove(familyDetailsList.get(position))
        }

    }

    override fun getItemCount(): Int {
        return familyDetailsList.size
    }
}