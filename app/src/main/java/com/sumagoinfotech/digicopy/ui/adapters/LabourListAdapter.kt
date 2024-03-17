package com.sumagoinfotech.digicopy.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.model.LabourWithAreaNames
import com.sumagoinfotech.digicopy.ui.activities.ViewLabourDetailsActivity
import com.sumagoinfotech.digicopy.ui.activities.registration.LabourRegistrationEdit1

class LabourListAdapter(var list: List<LabourWithAreaNames>) : RecyclerView.Adapter<LabourListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val ivEdit=itemView.findViewById<ImageView>(R.id.ivEdit)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
        val ivView=itemView.findViewById<ImageView>(R.id.ivView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabourListAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_labour_list,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabourListAdapter.ViewHolder, position: Int) {
        holder.ivPhoto.setImageURI(Uri.parse(list[position].photo))
        holder.tvFullName.text = list[position]?.fullName ?: "Default"
        holder.tvMobile.text = list[position]?.mobile ?: "Default"
        val address="${list[position].districtName} ->${list[position].talukaName} ->${list[position].villageName}"
        holder.tvAddress.text = address
        holder.tvMgnregaId.text= list[position].mgnregaId
        holder.ivEdit.setOnClickListener {
            val intent= Intent(holder.itemView.context,LabourRegistrationEdit1::class.java)
            intent.putExtra("id",list[position].id.toString())
            holder.itemView.context.startActivity(intent)
        }
        holder.ivView.setOnClickListener {
            val intent= Intent(holder.itemView.context,ViewLabourDetailsActivity::class.java)
            intent.putExtra("id",list[position].id.toString())
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun createUriFromPath(path: String): Uri {
        return Uri.Builder()
            .path(path)
            .build()
    }
}