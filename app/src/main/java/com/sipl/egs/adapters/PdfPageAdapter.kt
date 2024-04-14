package com.sipl.egs.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs.R
import java.io.File
import java.io.IOException

class PdfPageAdapter(private val context: Context, private val pdfFile: File, private val onDeletePageListener: OnDeletePageListener,private val onPdfPageClickListener: OnPdfPageClickListener) :
    RecyclerView.Adapter<PdfPageAdapter.PdfPageViewHolder>() {

    private val renderer: PdfRenderer
    private val parcelFileDescriptor: ParcelFileDescriptor

    interface OnDeletePageListener {
        fun onDeletePage(pageIndex: Int)
    }

    interface OnPdfPageClickListener {
        fun onPdfPageClick(bitmap: Bitmap)
    }

    init {
        parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(parcelFileDescriptor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pdf_page, parent, false)
        return PdfPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        val page = renderer.openPage(position)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        holder.imageView.setImageBitmap(bitmap)
        page.close()

        holder.tvPageNumber.setText(""+(position+1))
        if(position==0){
            holder.ivDelete.visibility= View.GONE
        }else{
            holder.ivDelete.setOnClickListener {
                onDeletePageListener.onDeletePage(position)
            }
        }

        holder.itemView.setOnClickListener {
            onPdfPageClickListener.onPdfPageClick(bitmap)
        }

    }

    override fun getItemCount(): Int {
        return renderer.pageCount
    }

    class PdfPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
        val tvPageNumber: TextView = itemView.findViewById(R.id.tvPageNumber)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        try {
            renderer.close()
            parcelFileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}