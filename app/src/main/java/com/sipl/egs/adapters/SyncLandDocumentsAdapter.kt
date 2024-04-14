package com.sipl.egs.adapters

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sipl.egs.R
import com.sipl.egs.database.entity.Document
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class SyncLandDocumentsAdapter(var documentList: List<Document>) : RecyclerView.Adapter<SyncLandDocumentsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPageCount=itemView.findViewById<TextView>(R.id.tvPageCount)
        val tvDocumentDate=itemView.findViewById<TextView>(R.id.tvDocumentDate)
        val tvDocumentName=itemView.findViewById<TextView>(R.id.tvDocumentName)
        val tvDocumentType=itemView.findViewById<TextView>(R.id.tvDocumentType)
        val ivDocumentThumb=itemView.findViewById<ImageView>(R.id.ivDocumentThumb)
        val layoutWrapper=itemView.findViewById<LinearLayout>(R.id.layoutWrapper)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_sync_land_document,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.layoutWrapper.setBackgroundColor(Color.parseColor(documentList[position].doc_color))
            holder.tvPageCount.setBackgroundColor(Color.parseColor(documentList[position].doc_color))
            holder.tvDocumentName.text=documentList[position].documentName
            holder.tvDocumentDate.text=formatDate(documentList[position].date)
            holder.tvPageCount.text=documentList[position].pageCount
            holder.tvDocumentType.text=documentList[position].documentTypeName
            holder.itemView.setOnClickListener {
                val file=File(Uri.parse(documentList[position].documentUri).path)
                openPdfFromUri(holder.itemView.context,file)
            }
            val bitmap=generateThumbnailFromPDF(documentList[position].documentUri,holder.itemView.context)
            Glide.with(holder.itemView.context).load(bitmap).into(holder.ivDocumentThumb)
        } catch (e: Exception) {
            Log.d("mytag","Exception : onBindViewHolder "+e.message)
            Log.d("mytag","SyncLandDocumentsAdapter: ${e.message}",e)

            e.printStackTrace()
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
        }catch (e: ActivityNotFoundException) {
            Log.d("mytag","SyncLandDocumentsAdapter: ${e.message}",e)
        }
    }
    private fun generateThumbnailFromPDF(pdfUriStr: String?, context: Context): Bitmap? {
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
            Log.d("mytag","SyncLandDocumentsAdapter: ${e.message}",e)
        } finally {
            try {
                pdfFileDescriptor?.close()
            } catch (e: Exception) {
                Log.d("mytag","Exception : generateThumbnailFromPDF "+e.message)
                Log.d("mytag","SyncLandDocumentsAdapter: ${e.message}",e)
                e.printStackTrace()
            }
        }
        return null

    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("mytag","UploadedPdfListAdapter: ${e.message}",e)
            "Invalid Date"
        }
    }
}