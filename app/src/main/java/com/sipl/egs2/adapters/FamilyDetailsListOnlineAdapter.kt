package com.sipl.egs2.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs2.R
import com.sipl.egs2.model.apis.getlabour.FamilyDetail

class FamilyDetailsListOnlineAdapter(var familyDetailsList: List<FamilyDetail>?) :
    RecyclerView.Adapter<FamilyDetailsListOnlineAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvDob=itemView.findViewById<TextView>(R.id.tvDob)
        val tvRelationship=itemView.findViewById<TextView>(R.id.tvRelationship)
        val tvMaritalStatus=itemView.findViewById<TextView>(R.id.tvMaritalStatus)
        val tvGender=itemView.findViewById<TextView>(R.id.tvGender)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_view_family_details,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        try {
            holder.tvFullName.setText(familyDetailsList?.get(position)?.full_name)
            holder.tvDob.setText(familyDetailsList?.get(position)?.date_of_birth)
            holder.tvRelationship.setText(familyDetailsList?.get(position)?.relation)
            holder.tvMaritalStatus.setText(familyDetailsList?.get(position)?.maritalStatus)
            holder.tvGender.setText(familyDetailsList?.get(position)?.gender)
        } catch (e: Exception) {
            Log.d("mytag","FamilyDetailsListOnlineAdapter: ${e.message}",e)
        }
    }

    override fun getItemCount(): Int {
        return familyDetailsList!!.size
    }
}