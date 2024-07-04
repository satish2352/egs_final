package com.sipl.egs2.adapters

import android.annotation.SuppressLint
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
import com.sipl.egs2.model.apis.attendance.AttendanceData
import com.sipl.egs2.ui.gramsevak.ViewLabourFromMarkerClick
import com.sipl.egs2.utils.MySharedPref
import java.text.SimpleDateFormat
import java.util.Date

class OfficerLabourListByProjectIdOfAttendance(var list: List<AttendanceData>?) :
    RecyclerView.Adapter<OfficerLabourListByProjectIdOfAttendance.ViewHolder>() {
    lateinit var pref: MySharedPref
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
    ): OfficerLabourListByProjectIdOfAttendance.ViewHolder {
        pref= MySharedPref(parent.context)
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_labour_list_by_project_with_attendance,parent,false)
        return OfficerLabourListByProjectIdOfAttendance.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OfficerLabourListByProjectIdOfAttendance.ViewHolder,
        position: Int
    ) {

        try {
            holder.tvFullName.text = list?.get(position)?.full_name ?: "Default"
            holder.tvMobile.text = list?.get(position)?.mobile_number ?: "Default"
            val address="${list?.get(position)?.project_name}"
            holder.tvAddress.text = address
            holder.tvMgnregaId.text= list?.get(position)?.mgnrega_card_id!!
            Glide.with(holder.itemView.context).load(list!!.get(position).profile_image).override(100,100).into(holder.ivPhoto)
            if(list!!.get(position).attendance_day.equals("half_day")){
                holder.tvAttendance.setText("Half Day")
            }else if(list!!.get(position).attendance_day.equals("full_day")){
                holder.tvAttendance.setText("Full Day")
            }

            holder.tvDate.setText(formatDate(list?.get(position)?.updated_at!!))

            holder.itemView.setOnClickListener {
                    val intent= Intent(holder.itemView.context, ViewLabourFromMarkerClick::class.java)
                    intent.putExtra("id",list?.get(position)?.mgnrega_card_id)
                intent.putExtra("labour_id",list?.get(position)?.id)

                holder.itemView.context?.startActivity(intent)

            }

        } catch (e: Exception) {
            Log.d("mytag","OfficerLabourListByProjectIdOfAttendance: ${e.message}",e)
            Log.d("mytag","Exception : OfficerLabourListByProjectIdOfAttendance Adapter => "+e.message)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        Log.d("mytag",""+list?.size!!)
        return list?.size!!
    }
    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("mytag","UploadedPdfListAdapter: ${e.message}",e)
            "Invalid Date"
        }
    }
}