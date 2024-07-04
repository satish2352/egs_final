package com.sipl.egs2.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egs2.R
import com.sipl.egs2.interfaces.MarkAttendanceListener
import com.sipl.egs2.model.apis.getlabour.LabourInfo

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
    ): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_attendance_recyclerview,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            holder.tvFullName.text = list[position]?.full_name ?: "Default"
            holder.tvMobile.text = list[position]?.mobile_number ?: "Default"
            val address="${list[position].district_name} ->${list[position].taluka_name} ->${list[position].village_name}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text= list[position].mgnrega_card_id
            holder.itemView.setOnClickListener{

                markAttendanceListener.markAttendance(list[position])
            }
            Glide.with(holder.itemView.context).load(list[position].profile_image).into(holder.ivPhoto)
            Log.d("mytag",list.get(position).profile_image)
        } catch (e: Exception) {
            Log.d("mytag","AttendanceAdapter:",e)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}