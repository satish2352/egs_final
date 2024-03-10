package com.sumagoinfotech.digicopy.ui.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.entity.User

class LabourReportsAdapter(var list: List<User>) : RecyclerView.Adapter<LabourReportsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabourReportsAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_reports_recyclerview,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabourReportsAdapter.ViewHolder, position: Int) {

        Log.d("mytag",""+createUriFromPath(list.get(position).photo))

        holder.ivPhoto.setImageURI(Uri.parse(list.get(position).photo))
        holder.tvFullName.setText(list.get(position)?.fullName ?: "Default")
        holder.tvMobile.setText(list.get(position)?.mobile ?: "Default")
        val address="${list.get(position).district} ->${list.get(position).taluka} ->${list.get(position).village}"
        holder.tvAddress.setText(address)
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