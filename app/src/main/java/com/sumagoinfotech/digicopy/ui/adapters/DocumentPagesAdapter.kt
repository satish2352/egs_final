package com.sumagoinfotech.digicopy.ui.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.interfaces.UpdateDocumentTypeListener
import java.io.File

class DocumentPagesAdapter(var documentList: List<Document>,var updateDocumentTypeListener: UpdateDocumentTypeListener) : RecyclerView.Adapter<DocumentPagesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val textViewPageCount=itemView.findViewById<TextView>(R.id.textViewPageCount)
        val ivDeleteDocument=itemView.findViewById<ImageView>(R.id.tvDeleteDocument)
        val ivThumb=itemView.findViewById<ImageView>(R.id.ivThumb)
        val tvDocumentName=itemView.findViewById<TextView>(R.id.tvDocumentName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentPagesAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_document_pages,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentPagesAdapter.ViewHolder, position: Int)
    {
        holder.tvDocumentName.text=documentList.get(position).documentName
        holder.ivThumb.setOnClickListener {

            val file=File(Uri.parse(documentList[position].documentUri).path)
            openPdfFromUri(holder.itemView.context,file)
        }
        holder.textViewPageCount.text= documentList[position].pageCount
        holder.ivDeleteDocument.setOnClickListener {
            Log.d("mytag","deleteHere")
            updateDocumentTypeListener.onUpdateDocumentType(documentList.get(position))
        }

    }

    override fun getItemCount(): Int {
        return documentList.size
    }
    fun openPdfFromUri(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle scenario where PDF viewer application is not found
            Toast.makeText(context, "No PDF viewer application found", Toast.LENGTH_SHORT).show()
        }
    }

}