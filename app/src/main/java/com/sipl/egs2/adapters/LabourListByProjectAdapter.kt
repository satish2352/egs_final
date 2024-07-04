package com.sipl.egs2.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egs2.R
import com.sipl.egs2.model.apis.getlabour.LabourInfo
import com.sipl.egs2.ui.gramsevak.ViewLabourFromMarkerClick

class LabourListByProjectAdapter(var list: List<LabourInfo>?) : RecyclerView.Adapter<LabourListByProjectAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_labour_list_by_project,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            holder.tvFullName.text = list?.get(position)?.full_name ?: "Default"
            holder.tvMobile.text = list?.get(position)?.mobile_number ?: "Default"
            val address="${list?.get(position)?.district_name} ->${list?.get(position)?.taluka_name} ->${list?.get(position)?.village_name}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text= list?.get(position)?.mgnrega_card_id
            Glide.with(holder.itemView.context).load(list?.get(position)?.profile_image).override(100,100).into(holder.ivPhoto)

            holder.itemView.setOnClickListener {
                val intent= Intent(holder.itemView.context, ViewLabourFromMarkerClick::class.java)
                intent.putExtra("id",list?.get(position)?.mgnrega_card_id)
                intent.putExtra("labour_id",list?.get(position)?.id)
                holder.itemView.context?.startActivity(intent)
            }

        } catch (e: Exception) {
            Log.d("mytag","LabourListByProjectAdapter: ${e.message}",e)
        }
    }

    override fun getItemCount(): Int {
        return list!!.size!!
    }
}