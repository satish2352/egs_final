package com.sipl.egs2.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs2.R
import com.sipl.egs2.interfaces.OnDownloadDocumentClickListener
import com.sipl.egs2.model.apis.uploadeddocs.UploadedDocument
import java.text.SimpleDateFormat
import java.util.Date

class OfficerUploadedDocsAdapter(var documentList:List<UploadedDocument>,var onDownloadDocumentClickListener: OnDownloadDocumentClickListener):RecyclerView.Adapter<OfficerUploadedDocsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvDownload=itemView.findViewById<ImageView>(R.id.tvDownload)
        val tvDocumentDate=itemView.findViewById<TextView>(R.id.tvDocumentDate)
        val tvDocumentType=itemView.findViewById<TextView>(R.id.tvDocumentType)
        val tvDocumentName=itemView.findViewById<TextView>(R.id.tvDocumentName)
        val ivDocumentThumb=itemView.findViewById<ImageView>(R.id.ivDocumentThumb)
        val layoutWrapper=itemView.findViewById<LinearLayout>(R.id.layoutWrapper)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OfficerUploadedDocsAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_uploaded_pdf,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficerUploadedDocsAdapter.ViewHolder, position: Int) {
        try {
            holder.tvDownload.imageTintList= ColorStateList.valueOf(Color.parseColor(documentList[position].doc_color))
            holder.layoutWrapper.setBackgroundColor(Color.parseColor(documentList[position].doc_color))
            holder.tvDocumentName.text=documentList[position].document_name
            holder.itemView.setOnClickListener {

            }
            holder.tvDocumentDate.setText(formatDate(documentList.get(position).updated_at))
            holder.tvDocumentType.setText(documentList.get(position).document_type_name)
            holder.tvDownload.setOnClickListener {

                onDownloadDocumentClickListener.onDownloadDocumentClick(documentList.get(position).document_pdf,documentList.get(position).document_name)
                //FileDownloader.downloadFile(holder.itemView.context,documentList.get(position).document_pdf,documentList.get(position).document_name)
            }
        } catch (e: Exception) {
            Log.d("mytag","OfficerUploadedDocsAdapter: ${e.message}",e)
        }
    }

    override fun getItemCount(): Int {
        return documentList.size
    }
    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("mytag","OfficerUploadedDocsAdapter: ${e.message}",e)
            "Invalid Date"
        }
    }
}