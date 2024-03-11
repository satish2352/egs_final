package com.sumagoinfotech.digicopy.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.interfaces.MarkAttendanceListener

class AttendanceAdapter(var list: List<Labour>, var markAttendanceListener: MarkAttendanceListener): RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttendanceAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_attendance_recyclerview,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceAdapter.ViewHolder, position: Int) {
        holder.ivPhoto.setImageURI(Uri.parse(list[position].photo))
        holder.tvFullName.text = list[position]?.fullName ?: "Default"
        holder.tvMobile.text = list[position]?.mobile ?: "Default"
        val address="${list[position].district} ->${list[position].taluka} ->${list[position].village}"
        holder.tvAddress.text = address
        holder.tvMgnregaId.text= list[position].mgnregaId
        holder.itemView.setOnClickListener{

            markAttendanceListener.markAttendance(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}