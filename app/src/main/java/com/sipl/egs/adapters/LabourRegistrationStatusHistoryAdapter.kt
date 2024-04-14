package com.sipl.egs.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs.R

class LabourRegistrationStatusHistoryAdapter(var list:List<Any>): RecyclerView.Adapter<LabourRegistrationStatusHistoryAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        val tvDate:TextView=itemView.findViewById<TextView>(R.id.tvDate)
        val tvRemark:TextView=itemView.findViewById<TextView>(R.id.tvRemark)
        val tvReason: TextView =itemView.findViewById<TextView>(R.id.tvReason)
        val tvStatus:TextView=itemView.findViewById<TextView>(R.id.tvStatus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabourRegistrationStatusHistoryAdapter.ViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_history_user_status,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: LabourRegistrationStatusHistoryAdapter.ViewHolder, position: Int) {

    }
    override fun getItemCount(): Int {
        return list.size
    }
}