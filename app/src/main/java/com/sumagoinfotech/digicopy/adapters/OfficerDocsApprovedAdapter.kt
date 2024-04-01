package com.sumagoinfotech.digicopy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R

class OfficerDocsApprovedAdapter (var list:MutableList<Any>):
    RecyclerView.Adapter<OfficerDocsApprovedAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvDocumentName: TextView =itemView.findViewById(R.id.tvDocumentName)
        val tvDocumentType: TextView =itemView.findViewById(R.id.tvDocumentType)
        val tvDocumentStatus: TextView =itemView.findViewById(R.id.tvDocumentStatus)
        val tvDocumentDate: TextView =itemView.findViewById(R.id.tvDocumentDate)
        val tvAddress: TextView =itemView.findViewById(R.id.tvAddress)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OfficerDocsApprovedAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_document_received_for_approval,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OfficerDocsApprovedAdapter.ViewHolder,
        position: Int
    ) {

    }

    override fun getItemCount(): Int {

        return list.size;
    }
}