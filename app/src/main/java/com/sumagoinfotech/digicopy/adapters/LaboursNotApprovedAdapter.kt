package com.sumagoinfotech.digicopy.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.model.apis.labourlist.LaboursList
import com.sumagoinfotech.digicopy.ui.gramsevak.ViewNotApprovedLabourDetailsActivity
import com.sumagoinfotech.digicopy.ui.officer.activities.OfficerViewNotApprovedLabourDetails
import com.sumagoinfotech.digicopy.utils.MySharedPref

class LaboursNotApprovedAdapter(var labourList: ArrayList<LaboursList>) :
    RecyclerView.Adapter<LaboursNotApprovedAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvFullName = itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile = itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto = itemView.findViewById<ImageView>(R.id.ivPhoto)
        val tvMgnregaId = itemView.findViewById<TextView>(R.id.tvMgnregaId)
        val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
        val tvReason = itemView.findViewById<TextView>(R.id.tvReason)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LaboursNotApprovedAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_labours_not_approved, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LaboursNotApprovedAdapter.ViewHolder, position: Int) {

        try {
            holder.tvFullName.text = labourList[position]?.full_name ?: ""
            holder.tvMobile.text = labourList[position]?.mobile_number ?: ""
            val address =
                "${labourList[position].district_name} ->${labourList[position].taluka_name} ->${labourList[position].village_name}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text = labourList[position].mgnrega_card_id
            holder.tvStatus.text = labourList[position].status_name
            Glide.with(holder.itemView.context).load(labourList[position].profile_image)
                .into(holder.ivPhoto)
            holder.itemView.setOnClickListener {
                val pref = MySharedPref(holder.itemView.context)
                if (pref.getRoleId() == 2) {
                    val intent = Intent(holder.itemView.context, OfficerViewNotApprovedLabourDetails::class.java)
                    intent.putExtra("id", labourList.get(position).mgnrega_card_id)
                    holder.itemView.context.startActivity(intent)
                }
                if (pref.getRoleId() == 3) {
                    val intent = Intent(holder.itemView.context, ViewNotApprovedLabourDetailsActivity::class.java)
                    intent.putExtra("id", labourList.get(position).mgnrega_card_id)
                    holder.itemView.context.startActivity(intent)
                }
            }

        } catch (e: Exception) {
            Log.d("mytag", "LaboursSentForApprovalAdapter:onBindViewHolder  " + e.message)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {

        return labourList.size
    }
}