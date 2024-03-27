package com.sumagoinfotech.digicopy.adapters

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
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
import com.sumagoinfotech.digicopy.utils.MySharedPref
import java.text.SimpleDateFormat
import java.util.Date

class ViewAttendanceAdapter(var list:List<AttendanceData>,var attendanceEditListener: AttendanceEditListener): RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder>() {

    lateinit var pref:MySharedPref
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvFullName=itemView.findViewById<TextView>(R.id.tvFullName)
        val tvAddress=itemView.findViewById<TextView>(R.id.tvAddress)
        val tvMobile=itemView.findViewById<TextView>(R.id.tvMobile)
        val ivPhoto=itemView.findViewById<ImageView>(R.id.ivPhoto)
        val tvMgnregaId=itemView.findViewById<TextView>(R.id.tvMgnregaId)
        val tvAttendance=itemView.findViewById<TextView>(R.id.tvAttendance)
        val ivEdit=itemView.findViewById<ImageView>(R.id.ivEdit)
        val tvDate=itemView.findViewById<TextView>(R.id.tvDate)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        pref= MySharedPref(parent.context)
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_view_marked_attendance_list,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            holder.tvFullName.text = list[position]?.full_name ?: "Default"
            holder.tvMobile.text = list[position]?.mobile_number ?: "Default"
            val address="${list[position].project_name}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text= list[position].mgnrega_card_id
            Glide.with(holder.itemView.context).load(list.get(position).profile_image).into(holder.ivPhoto)
            holder.tvAttendance.setText(list.get(position).attendance_day)
            holder.tvDate.setText(formatDate(list.get(position).updated_at))
            if(pref.getRoleId()==2)
            {
                holder.ivEdit.visibility=View.GONE
            }else{
                holder.ivEdit.setOnClickListener {
                    attendanceEditListener.onAttendanceEdit(list.get(position),position)
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception : Attendance Adapter => "+e.message)
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}