package com.sipl.egs2.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs2.R
import com.sipl.egs2.model.apis.getlabour.HistoryDetailsItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrationStatusHistoryAdapter(var list:ArrayList<HistoryDetailsItem>):RecyclerView.Adapter<RegistrationStatusHistoryAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        val tvStatus=itemView.findViewById<TextView>(R.id.tvStatus)
        val tvReason=itemView.findViewById<TextView>(R.id.tvReason)
        val tvRemark=itemView.findViewById<TextView>(R.id.tvRemark)
        val tvDate=itemView.findViewById<TextView>(R.id.tvDate)
        val tvLabelRemarks=itemView.findViewById<TextView>(R.id.tvLabelRemarks)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RegistrationStatusHistoryAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.row_item_history_registration_status,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RegistrationStatusHistoryAdapter.ViewHolder,
        position: Int
    ) {

        try {
            if(!list[position].other_remark.equals("null"))
            {
                holder.tvRemark.text=list[position].other_remark
                holder.tvRemark.visibility=View.GONE
                holder.tvLabelRemarks.visibility=View.GONE
            }
            if(list[position].other_remark==null){

                holder.tvRemark.visibility=View.GONE
                holder.tvLabelRemarks.visibility=View.GONE
            }else{
                holder.tvRemark.visibility=View.VISIBLE
                holder.tvLabelRemarks.visibility=View.VISIBLE
            }
            holder.tvReason.text=list[position].reason_name
            holder.tvDate.text=formatDate(list[position].updated_at)
        } catch (e: Exception) {
            Log.d("mytag","RegistrationStatusHistoryAdapter: ${e.message}",e)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("mytag","RegistrationStatusHistoryAdapter: ${e.message}",e)
            "Invalid Date"
        }
    }
}