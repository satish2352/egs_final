package com.sumagoinfotech.digicopy.utils
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
object PDFThumbnailLoader {

    fun loadPDFThumbnail(context: Context, pdfUrl: String, imageView: ImageView) {
        val options = RequestOptions()
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the PDF file

        Glide.with(context)
            .`as`(Bitmap::class.java)
            .load(pdfUrl) // Load PDF from URL
            .apply(options)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Bitmap loaded successfully, set it to ImageView
                    imageView.setImageBitmap(resource)
                }
            })
    }
}