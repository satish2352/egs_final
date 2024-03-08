package com.sumagoinfotech.digicopy.ui.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sumagoinfotech.digicopy.databinding.ActivityImageCaptureBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageCaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageCaptureBinding
    lateinit var capturedImageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.captureButton.setOnClickListener {
             capturedImageUri =
                 contentResolver.insert(
                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                     ContentValues()
                 )!!
             val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
             intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)
             launcher.launch(intent)
        }
    }
    var launcher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    Log.d("mytag", "" + capturedImageUri)
                    val savedFile = saveImageToDownloads(
                        this@ImageCaptureActivity,
                        capturedImageUri, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
                    if (savedFile != null) {
                        Log.d("mytag", "Image saved to Downloads: ${savedFile.absolutePath}")
                    } else {
                        Log.d("mytag", "Failed to save image to Downloads")
                    }
                }
            })

    fun saveImageToDownloads(context: Context, uri: Uri, text: String): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        val bitmap = BitmapFactory.decodeStream(inputStream)
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            textSize = 100f
            isFakeBoldText = true // Set text to bold
        }

        val textWidth = paint.measureText(text)
        val x = (mutableBitmap.width - textWidth - 20) // 20 pixels padding from the right
        val y = (mutableBitmap.height - 20) // 20 pixels padding from the bottom

        canvas.drawText(text, x, y.toFloat(), paint)

        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val savedFile = File(downloadsDir, fileName)

        try {
            FileOutputStream(savedFile).use { outputStream ->
                mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            return savedFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            inputStream.close()
        }
    }
}