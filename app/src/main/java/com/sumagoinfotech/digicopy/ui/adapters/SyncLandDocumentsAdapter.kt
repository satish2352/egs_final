package com.sumagoinfotech.digicopy.ui.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.utils.PDFThumbnailLoader
import java.io.File

class SyncLandDocumentsAdapter(var documentList: List<Document>) : RecyclerView.Adapter<SyncLandDocumentsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPageCount=itemView.findViewById<TextView>(R.id.tvPageCount)
        val tvDocumentDate=itemView.findViewById<TextView>(R.id.tvDocumentDate)
        val tvDocumentName=itemView.findViewById<TextView>(R.id.tvDocumentName)
        val ivDocumentThumb=itemView.findViewById<ImageView>(R.id.ivDocumentThumb)


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SyncLandDocumentsAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_row_sync_land_document,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SyncLandDocumentsAdapter.ViewHolder, position: Int) {
        try {
            holder.tvDocumentName.text=documentList[position].documentName
            holder.tvPageCount.text=documentList[position].pageCount
            holder.itemView.setOnClickListener {
                val file=File(Uri.parse(documentList[position].documentUri).path)
                openPdfFromUri(holder.itemView.context,file)
            }
            val bitmap=generateThumbnailFromPDF(documentList[position].documentUri,holder.itemView.context)
            Glide.with(holder.itemView.context).load(bitmap).into(holder.ivDocumentThumb)
        } catch (e: Exception) {

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

    /*fun generatePdfThumbnail(pdfPath: String): Bitmap? {
        var pdfDocument: PdfDocument? = null
        var outputStream: ByteArrayOutputStream? = null

        try {
            val pdfReader = PdfReader(pdfPath)
            pdfDocument = PdfDocument(pdfReader)

            outputStream = ByteArrayOutputStream()
            val imageRenderListener = ImageRenderListener()
            val processor = PdfCanvasProcessor(imageRenderListener)

            // Process the first page of the PDF
            processor.processPageContent(pdfDocument.getPage(1))

            // Get the image data
            val imageData = imageRenderListener.getImage()

            // Convert image data to bytes
            val bytes = imageData?.image?.data

            if (bytes != null) {
                // Convert bytes to Bitmap
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (e: Exception) {
            Log.e("PDF Thumbnail", "Error generating PDF thumbnail", e)
        } finally {
            pdfDocument?.close()
            outputStream?.close()
        }

        return null
    }*/

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