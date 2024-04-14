package com.sipl.egs.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egs.R
import com.sipl.egs.database.entity.Document
import com.sipl.egs.interfaces.UpdateDocumentTypeListener
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
    ): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_document_pages,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        Log.d("mytag","onBindViewHolder=>"+documentList.get(position).documentName)
        holder.tvDocumentName.text=documentList.get(position).documentName
        holder.ivThumb.setOnClickListener {

            val file=File(Uri.parse(documentList[position].documentUri).path)
            openPdfFromUri(holder.itemView.context,file)
        }
        holder.textViewPageCount.text= documentList[position].pageCount
        holder.ivDeleteDocument.setOnClickListener {
            updateDocumentTypeListener.onUpdateDocumentType(documentList.get(position))
        }
        val bitmap=generateThumbnailFromPDF(documentList[position].documentUri,holder.itemView.context)
        Glide.with(holder.itemView.context).load(bitmap).into(holder.ivThumb)
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
    fun generateThumbnailFromPDF(pdfUriStr: String?,context: Context): Bitmap? {
        var pdfRenderer: PdfRenderer? = null;
        var pdfFileDescriptor: ParcelFileDescriptor? = null;
        try {
            //Open the PDF file descriptor
            //here "r" = read.
            pdfFileDescriptor =
                context.contentResolver.openFileDescriptor(Uri.parse(pdfUriStr), "r")
            if (pdfFileDescriptor != null) {
                // Create a PdfRenderer from the file descriptor
                pdfRenderer = PdfRenderer(pdfFileDescriptor)
                // Ensure the page index is within bounds
                // i want to create page 0 thumbnail so given 0 if you want  other you can give according to you
                val pageIndex = 1;
                if (pageIndex < pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(pageIndex)
                    // Create a bitmap for the thumbnail
                    val thumbnail = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                    // Render the page to the bitmap
                    page.render(thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    // Close the page and renderer
                    page.close()
                    pdfRenderer.close()
                    return thumbnail

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                pdfFileDescriptor?.close()
            } catch (e: Exception) {

            }
        }
        return null

    }


}