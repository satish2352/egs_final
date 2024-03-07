package com.sumagoinfotech.digicopy.ui.activities


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.sumagoinfotech.digicopy.R
import java.util.concurrent.Executors

class ScanBarcodeActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private lateinit var cameraPreview: SurfaceView
    private lateinit var barcodeTextView: TextView
    private lateinit var barcodeScanner: BarcodeScanner
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_barcode)

        cameraPreview = findViewById(R.id.cameraPreview)
        barcodeTextView = findViewById(R.id.barcodeTextView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has already been granted
            initializeCamera()
        }
    }

    private fun initializeCamera() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient(options)

        cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startCamera()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // release resources
            }
        })
    }

    private fun startCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            cameraPreview.holder.setKeepScreenOn(true)
            cameraPreview.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {}

                override fun surfaceCreated(holder: SurfaceHolder) {
                    cameraScanner(holder)
                }
            })
        } catch (e: Exception) {
            Log.d("Exception", "startCamera: " + e.message)
        }
    }

    private fun cameraScanner(holder: SurfaceHolder) {
        val inputImage = InputImage.fromBitmap(cameraPreview.drawingCache, 0)
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue
                    val valueType = barcode.valueType

                    // Extract barcode data
                    val displayValue = barcode.displayValue
                    barcodeTextView.text = displayValue
                    barcodeTextView.visibility = TextView.VISIBLE
                }
            }
            .addOnFailureListener {
                // Task failed with an exception
                // ...
            }
            .addOnCompleteListener {
                cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                    override fun surfaceDestroyed(holder: SurfaceHolder) {}

                    override fun surfaceCreated(holder: SurfaceHolder) {
                        cameraScanner(holder)
                    }
                })
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the camera
                initializeCamera()
            } else {
                // Permission denied
                // You might want to show a message to the user indicating that permission is required
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
