package com.sumagoinfotech.digicopy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R

class SyncLandDocumentsAdapter : RecyclerView.Adapter<SyncLandDocumentsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SyncLandDocumentsAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_sync_land_document,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SyncLandDocumentsAdapter.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 8
    }
}