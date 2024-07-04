package com.sipl.egs2.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs2.R
import com.sipl.egs2.interfaces.OnDeleteListener
import com.sipl.egs2.model.apis.LaboureEditDetailsOnline.FamilyDetail

class FamilyDetailsOnlineEditAdapter(
    var familyDetailsList: ArrayList<FamilyDetail>, var deleteListener: OnDeleteListener
) :
    RecyclerView.Adapter<FamilyDetailsOnlineEditAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFullName = itemView.findViewById<TextView>(R.id.tvFullName)
        val tvDob = itemView.findViewById<TextView>(R.id.tvDob)
        val tvRelationship = itemView.findViewById<TextView>(R.id.tvRelationship)
        val tvMaritalStatus = itemView.findViewById<TextView>(R.id.tvMaritalStatus)
        val tvGender = itemView.findViewById<TextView>(R.id.tvGender)
        val imageViewDelete = itemView.findViewById<ImageView>(R.id.imageViewDelete)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FamilyDetailsOnlineEditAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_family_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FamilyDetailsOnlineEditAdapter.ViewHolder,
        position: Int
    ) {
        try {
            holder.tvFullName.setText(familyDetailsList?.get(position)?.full_name)
            holder.tvDob.setText(familyDetailsList?.get(position)?.date_of_birth)
            holder.tvRelationship.setText(familyDetailsList?.get(position)?.relation)
            holder.tvMaritalStatus.setText(familyDetailsList?.get(position)?.maritalStatus)
            holder.tvGender.setText(familyDetailsList?.get(position)?.gender)
            holder.imageViewDelete.setOnClickListener {
                deleteListener.onDelete(position)
                notifyItemRemoved(position)
            }
        } catch (e: Exception) {
            Log.d("mytag","FamilyDetailsOnlineEditAdapter: ${e.message}",e)
        }
    }

    override fun getItemCount(): Int {
        return familyDetailsList.size
    }
}