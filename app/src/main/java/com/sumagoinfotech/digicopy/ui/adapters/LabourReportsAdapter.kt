package com.sumagoinfotech.digicopy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R

class LabourReportsAdapter : RecyclerView.Adapter<LabourReportsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabourReportsAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_reports_recyclerview,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabourReportsAdapter.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 10
    }
}