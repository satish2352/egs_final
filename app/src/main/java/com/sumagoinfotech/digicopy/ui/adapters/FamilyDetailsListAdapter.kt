package com.sumagoinfotech.digicopy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.model.FamilyDetails

class FamilyDetailsListAdapter(var familyDetailsList: ArrayList<FamilyDetails>) : RecyclerView.Adapter<FamilyDetailsListAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvDob=itemView.findViewById<TextView>(R.id.tvDob)
        val tvRelationship=itemView.findViewById<TextView>(R.id.tvRelationship)
        val tvMaritalStatus=itemView.findViewById<TextView>(R.id.tvMaritalStatus)
        val tvGender=itemView.findViewById<TextView>(R.id.tvGender)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FamilyDetailsListAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_view_family_details,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyDetailsListAdapter.ViewHolder, position: Int) {

        holder.tvFullName.setText(familyDetailsList.get(position).fullName)
        holder.tvDob.setText(familyDetailsList.get(position).dob)
        holder.tvRelationship.setText(familyDetailsList.get(position).relationship)
        holder.tvMaritalStatus.setText(familyDetailsList.get(position).maritalStatus)
        holder.tvGender.setText(familyDetailsList.get(position).gender)

    }

    override fun getItemCount(): Int {
        return familyDetailsList.size

    }
}