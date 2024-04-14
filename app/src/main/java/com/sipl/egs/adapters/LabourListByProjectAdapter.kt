package com.sipl.egs.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egs.R
import com.sipl.egs.model.apis.getlabour.LabourInfo
import com.sipl.egs.ui.gramsevak.ViewLabourFromMarkerClick

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

        holder.tvFullName.text = list?.get(position)?.full_name ?: "Default"
        holder.tvMobile.text = list?.get(position)?.mobile_number ?: "Default"
        val address="${list?.get(position)?.district_name} ->${list?.get(position)?.taluka_name} ->${list?.get(position)?.village_name}"
        holder.tvAddress.text = address
        holder.tvMgnregaId.text= list?.get(position)?.mgnrega_card_id
        Glide.with(holder.itemView.context).load(list?.get(position)?.profile_image).into(holder.ivPhoto)

        holder.itemView.setOnClickListener {
            val intent= Intent(holder.itemView.context, ViewLabourFromMarkerClick::class.java)
            intent.putExtra("id",list?.get(position)?.mgnrega_card_id)
            holder.itemView.context?.startActivity(intent)
        }
        Glide.with(holder.itemView.context).load(list?.get(position)?.profile_image)
    }

    override fun getItemCount(): Int {
        return list!!.size!!
    }
}