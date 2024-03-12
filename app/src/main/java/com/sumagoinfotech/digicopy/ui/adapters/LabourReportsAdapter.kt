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
import com.sumagoinfotech.digicopy.ui.activities.registration.LabourRegistrationEdit1

class LabourReportsAdapter(var list: List<Labour>) : RecyclerView.Adapter<LabourReportsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val ivEdit=itemView.findViewById<ImageView>(R.id.ivEdit)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabourReportsAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_reports_recyclerview,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabourReportsAdapter.ViewHolder, position: Int) {
        holder.ivPhoto.setImageURI(Uri.parse(list[position].photo))
        holder.tvFullName.text = list[position]?.fullName ?: "Default"
        holder.tvMobile.text = list[position]?.mobile ?: "Default"
        val address="${list[position].district} ->${list[position].taluka} ->${list[position].village}"
        holder.tvAddress.text = address
        holder.tvMgnregaId.text= list[position].mgnregaId
        holder.ivEdit.setOnClickListener {
            val intent= Intent(holder.itemView.context,LabourRegistrationEdit1::class.java)
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