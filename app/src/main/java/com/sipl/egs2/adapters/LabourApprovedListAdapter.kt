package com.sipl.egs2.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egs2.R
import com.sipl.egs2.model.apis.labourlist.LaboursList
import com.sipl.egs2.ui.gramsevak.ViewLabourFromMarkerClick
import com.sipl.egs2.ui.officer.activities.OfficerViewNotApprovedLabourDetails
import com.sipl.egs2.utils.MySharedPref

class LabourApprovedListAdapter(var labourList: ArrayList<LaboursList>): RecyclerView.Adapter<LabourApprovedListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
        val tvGramsevakName = itemView.findViewById<TextView>(R.id.tvGramsevakName)
        val layoutGramsevakName = itemView.findViewById<LinearLayout>(R.id.layoutGramsevakName)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabourApprovedListAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_labours_list_approved,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabourApprovedListAdapter.ViewHolder, position: Int) {
        try {
            holder.tvFullName.text = labourList[position]?.full_name ?: ""
            holder.tvMobile.text = labourList[position]?.mobile_number ?: ""
            val address="${labourList[position].district_name} ->${labourList[position].taluka_name} ->${labourList[position].village_name}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text= labourList[position].mgnrega_card_id
            Glide.with(holder.itemView.context).load(labourList[position].profile_image).override(75,75).into(holder.ivPhoto)
            val pref = MySharedPref(holder.itemView.context)
            if(pref.getRoleId()==2){
                holder.tvGramsevakName.text=labourList[position].gramsevak_full_name
            }else{
                holder.layoutGramsevakName.visibility=View.GONE
            }
            holder.itemView.setOnClickListener {
                val pref = MySharedPref(holder.itemView.context)
                if (pref.getRoleId() == 2) {
                    val intent = Intent(holder.itemView.context, OfficerViewNotApprovedLabourDetails::class.java)
                    intent.putExtra("id", labourList.get(position).mgnrega_card_id)
                    intent.putExtra("labour_id",labourList.get(position).id)
                    holder.itemView.context.startActivity(intent)
                }
                if (pref.getRoleId() == 3) {
                    val intent= Intent(holder.itemView.context, ViewLabourFromMarkerClick::class.java)
                    intent.putExtra("id",labourList.get(position).mgnrega_card_id)
                    intent.putExtra("labour_id",labourList.get(position).id)
                    holder.itemView.context.startActivity(intent)
                }

            }
        } catch (e: Exception) {
            Log.d("mytag","LabourApprovedListAdapter: ${e.message}",e)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return labourList.size
    }
}