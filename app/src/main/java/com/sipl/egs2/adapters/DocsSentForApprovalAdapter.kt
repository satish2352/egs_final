package com.sipl.egs2.adapters

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs2.R
import com.sipl.egs2.interfaces.OnDownloadDocumentClickListener
import com.sipl.egs2.model.apis.maindocsmodel.DocumentItem
import java.text.SimpleDateFormat
import java.util.Date

class DocsSentForApprovalAdapter (var list:MutableList<DocumentItem>,var onDownloadDocumentClickListener: OnDownloadDocumentClickListener):
    RecyclerView.Adapter<DocsSentForApprovalAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDocumentName: TextView =itemView.findViewById(R.id.tvDocumentName)
        val tvDocumentType: TextView =itemView.findViewById(R.id.tvDocumentType)
        val tvDocumentStatus: TextView =itemView.findViewById(R.id.tvDocumentStatus)
        val tvDocumentDate: TextView =itemView.findViewById(R.id.tvDocumentDate)
        val tvAddress: TextView =itemView.findViewById(R.id.tvAddress)
        val ivViewDocument: ImageView =itemView.findViewById(R.id.ivViewDocument)
        val ivDownloadDocument: ImageView =itemView.findViewById(R.id.ivDownloadDocument)
        val layoutWrapper: LinearLayout =itemView.findViewById(R.id.layoutWrapper)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocsSentForApprovalAdapter.ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_row_document_sent_for_approval,parent,false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(
        holder: DocsSentForApprovalAdapter.ViewHolder,
        position: Int
    )
    {
        try {
            holder.layoutWrapper.setBackgroundColor(Color.parseColor(list[position].doc_color))
            holder.tvDocumentName.setText(list[position].document_name)
            holder.tvDocumentType.setText(list[position].document_type_name)
            holder.tvDocumentStatus.setText(list[position].status_name)
            holder.tvDocumentDate.setText(formatDate(list[position].updated_at))
            holder.tvAddress.setText(list[position].district_name+"->"+list[position].taluka_name+"->"+list[position].village_name)
            holder.ivViewDocument.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(list[position].document_pdf), "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    holder.itemView.context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        holder.itemView.context, "No PDF viewer application found", Toast.LENGTH_LONG
                    ).show()
                }
            }
            holder.ivDownloadDocument.setOnClickListener {
                onDownloadDocumentClickListener.onDownloadDocumentClick(list.get(position).document_pdf,list.get(position).document_name)
               // FileDownloader.downloadFile(holder.itemView.context,list.get(position).document_pdf,list.get(position).document_name)
            }
        } catch (e: Exception) {
            Log.d("mytag","DocsSentForApprovalAdapter: ${e.message}",e)
        }
    }
    override fun getItemCount(): Int
    {
        return list.size;
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("mytag","DocsSentForApprovalAdapter: ${e.message}",e)
            ""
        }
    }
}