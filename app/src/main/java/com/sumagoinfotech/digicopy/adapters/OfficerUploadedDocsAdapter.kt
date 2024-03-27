package com.sumagoinfotech.digicopy.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.model.apis.uploadeddocs.UploadedDocument
import com.sumagoinfotech.digicopy.utils.FileDownloader
import java.text.SimpleDateFormat
import java.util.Date

class OfficerUploadedDocsAdapter(var documentList:List<UploadedDocument>):RecyclerView.Adapter<OfficerUploadedDocsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val tvDownload=itemView.findViewById<TextView>(R.id.tvDownload)
        val tvDocumentDate=itemView.findViewById<TextView>(R.id.tvDocumentDate)
        val tvDocumentType=itemView.findViewById<TextView>(R.id.tvDocumentType)
        val tvDocumentName=itemView.findViewById<TextView>(R.id.tvDocumentName)
        val ivDocumentThumb=itemView.findViewById<ImageView>(R.id.ivDocumentThumb)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OfficerUploadedDocsAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_uploaded_pdf,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfficerUploadedDocsAdapter.ViewHolder, position: Int) {
        holder.tvDocumentName.text=documentList[position].document_name
        holder.itemView.setOnClickListener {

        }
        holder.tvDocumentDate.setText(formatDate(documentList.get(position).updated_at))
        holder.tvDocumentType.setText(documentList.get(position).document_type_name)
        holder.tvDownload.setOnClickListener {
            /*val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(documentList.get(position).document_pdf), "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                holder.itemView.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Handle scenario where PDF viewer application is not found
                Toast.makeText(holder.itemView.context, "No PDF viewer application found", Toast.LENGTH_SHORT).show()
            }*/
            FileDownloader.downloadFile(holder.itemView.context,documentList.get(position).document_pdf,documentList.get(position).document_name)
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
            "Invalid Date"
        }
    }
}