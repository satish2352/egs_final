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
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourInfo

class AttendanceAdapter(var list: List<LabourInfo>, var markAttendanceListener: MarkAttendanceListener): RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {
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
        holder.ivPhoto.setImageURI(Uri.parse(list[position].profile_image))
        holder.tvFullName.text = list[position]?.full_name ?: "Default"
        holder.tvMobile.text = list[position]?.mobile_number ?: "Default"
        val address="${list[position].district_id} ->${list[position].taluka_id} ->${list[position].village_id}"
        holder.tvAddress.text = address
        holder.tvMgnregaId.text= list[position].mgnrega_card_id
        holder.itemView.setOnClickListener{

            //markAttendanceListener.markAttendance(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}