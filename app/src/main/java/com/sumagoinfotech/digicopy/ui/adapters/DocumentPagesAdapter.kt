package com.sumagoinfotech.digicopy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R

class DocumentPagesAdapter : RecyclerView.Adapter<DocumentPagesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val textViewPageCount=itemView.findViewById<TextView>(R.id.textViewPageCount)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentPagesAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_document_pages,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentPagesAdapter.ViewHolder, position: Int) {

        holder.textViewPageCount.text=(""+(position+1))

    }

    override fun getItemCount(): Int {
        return 8
    }
}