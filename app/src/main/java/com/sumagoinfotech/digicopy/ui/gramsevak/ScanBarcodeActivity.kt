package com.sumagoinfotech.digicopy.ui.gramsevak


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.databinding.ActivityScanBarcodeBinding
import com.sumagoinfotech.digicopy.model.apis.documetqrdownload.QRDocumentDownloadModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanBarcodeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityScanBarcodeBinding
    private val cameraPermission = android.Manifest.permission.CAMERA

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startScanner()
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestThePermissions()
        binding.buttonOpenScanner.setOnClickListener {
            requestCameraAndStartScanner()
            startScanner()
        }
        requestCameraAndStartScanner()
        startScanner()
    }
    private fun requestThePermissions() {

        PermissionX.init(this@ScanBarcodeActivity)
            .permissions(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION ,android.Manifest.permission.CAMERA)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    //Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                    //val dashboardFragment=DashboardFragment();
                    //dashboardFragment.updateMarker()
                } else {
                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                }
            }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraAndStartScanner() {
//        if (isPermissionGranted(cameraPermission)) {
//            startScanner()
//        } else {
//            requestCameraPermission()
//        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
//                cameraPermissionRequest(
//                    positive = { openPermissionSetting() }
//                )
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    private fun startScanner() {
        ScannerActivity.startScanner(this) { barcodes ->
            barcodes.forEach { barcode ->
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        binding.textViewQrType.text = "URL"
                        binding.textViewQrContent.text = barcode.url.toString()
                    }

                    Barcode.TYPE_CONTACT_INFO -> {
                        binding.textViewQrType.text = "Contact"
                        binding.textViewQrContent.text = barcode.contactInfo.toString()
                    }

                    else -> {
                        binding.textViewQrType.text = "Other"
                        binding.textViewQrContent.text = barcode.rawValue.toString()
                        getFileDownloadUrl(barcode.rawValue.toString())
                    }
                }
            }
        }
    }

    private fun getFileDownloadUrl(fileName:String){


        val dialog=CustomProgressDialog(this@ScanBarcodeActivity)
        dialog.show()
        val apiService=ApiClient.create(this@ScanBarcodeActivity)
        val call=apiService.downloadPDF(fileName)
        call.enqueue(object :Callback<QRDocumentDownloadModel>{
            override fun onResponse(call: Call<QRDocumentDownloadModel>, response: Response<QRDocumentDownloadModel>) {
                dialog.dismiss()
                if(response.isSuccessful){

                    if(response.body()?.status.equals("true")){
                        val url=response.body()?.data?.document_pdf.toString()
                        Log.d("mytag",url)
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(Uri.parse(url), "application/pdf")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                this@ScanBarcodeActivity,
                                "No PDF viewer application found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                       // FileDownloader.downloadFile(this@ScanBarcodeActivity,url,fileName)
                    }else{
                        Toast.makeText(this@ScanBarcodeActivity,"response false",Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this@ScanBarcodeActivity,"response unsuccessful",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<QRDocumentDownloadModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@ScanBarcodeActivity,"response failed",Toast.LENGTH_SHORT).show()
            }
        })

    }

}

