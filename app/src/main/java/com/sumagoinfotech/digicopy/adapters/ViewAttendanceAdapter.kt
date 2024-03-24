package com.sumagoinfotech.digicopy.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.interfaces.AttendanceEditListener
import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData

class ViewAttendanceAdapter(var list:List<AttendanceData>,var attendanceEditListener: AttendanceEditListener): RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
        val tvAttendance=itemView.findViewById<TextView>(R.id.tvAttendance)
        val ivEdit=itemView.findViewById<ImageView>(R.id.ivEdit)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_view_marked_attendance_list,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvFullName.text = list[position]?.full_name ?: "Default"
        holder.tvMobile.text = list[position]?.mobile_number ?: "Default"
        val address="${list[position].project_name}"
        holder.tvAddress.text = address
        holder.tvMgnregaId.text= list[position].mgnrega_card_id
        Glide.with(holder.itemView.context).load(list.get(position).profile_image).into(holder.ivPhoto)
        holder.tvAttendance.setText(list.get(position).attendance_day)

        holder.ivEdit.setOnClickListener {
            attendanceEditListener.onAttendanceEdit(list.get(position),position)
        }

    }
    override fun getItemCount(): Int {
        return list.size
    }
}