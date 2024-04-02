package com.sumagoinfotech.digicopy.ui.gramsevak.documents

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.RegistrationStatusHistoryAdapter
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentUpdateBinding
import com.sumagoinfotech.digicopy.model.apis.getlabour.HistoryDetailsItem
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.MainDocsModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.FileDownloader
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.FileInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Hashtable
import java.util.Locale

class DocumentUpdateActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDocumentUpdateBinding
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var scanner: GmsDocumentScanner
    private lateinit var dialog:CustomProgressDialog
    private var userDistrictName=""
    private var userTalukaName=""
    private var userVillageName=""
    private var userDocumentTypeName=""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private  var addressFromLatLong:String=""
    private  var isInternetAvailable=false
    private  var fileName=""
    private var documentUri=""
    private var gram_document_id=""
    private var prevFileName=""
    private var pdfUrl=""
    private var documentId=""
    private var historyList=ArrayList<HistoryDetailsItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDocumentUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title="Update Document"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tvHistory.visibility= View.GONE
        binding.recyclerViewHistory.visibility= View.GONE
        binding.layoutDocThumb.visibility= View.GONE
        binding.recyclerViewHistory.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        var adapter=RegistrationStatusHistoryAdapter(historyList)
        binding.recyclerViewHistory.adapter=adapter
        adapter.notifyDataSetChanged()
        gram_document_id=intent.getStringExtra("id").toString()
        dialog= CustomProgressDialog(this)
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(20)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        scanner = GmsDocumentScanning.getClient(options)
        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val result = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        /*result?.getPages()?.let { pages ->
                            for (page in pages) {
                            }
                            }*/
                    }
                    result?.getPdf()?.let { pdf ->
                        val pdfUri = pdf.uri
                        var pageCount = pdf.pageCount
                        pageCount += 1
                        val calendar = Calendar.getInstance()
                        val timeInMillis = convertTimeToCustomString(calendar.timeInMillis);
                        savePdfFileToStorage(pdfUri, pageCount.toString(), timeInMillis)
                    }
                }
            }
        binding.btnAddDocument.setOnClickListener {
            launchScanner()
        }
        binding.btnSubmit.setOnClickListener {
            uploadDocuments()
        }
        binding.ivViewDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this, "No PDF viewer application found", Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.ivDownloadDocument.setOnClickListener {
            FileDownloader.downloadFile(this,pdfUrl,prevFileName)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!isLocationEnabled()) {
            showEnableLocationDialog()
        } else {
            requestLocationUpdates()
        }
        checkAndPromptGps()
        getTheLocation()
        requestThePermissions()
    }
    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this@DocumentUpdateActivity)
        builder.setMessage("Location services are disabled. Do you want to enable them?")
            .setCancelable(false).setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                // Handle the case when the user refuses to enable location services
                Toast.makeText(
                    this@DocumentUpdateActivity,
                    "Unable to retrieve location without enabling location services",
                    Toast.LENGTH_LONG
                ).show()
            }
        val alert = builder.create()
        alert.show()
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this@DocumentUpdateActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@DocumentUpdateActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            ActivityCompat.requestPermissions(
                this@DocumentUpdateActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
            )
            Log.d("mytag", "requestLocationUpdates()  return ")
            return
        }
        Log.d("mytag", "requestLocationUpdates() ")

        // Request last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                latitude
            } ?: run {
                // Handle case where location is null
                Toast.makeText(
                    this@DocumentUpdateActivity, "Unable to retrieve location", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        getDocumentDetailsFromServer(gram_document_id);
    }



    private fun getDocumentDetailsFromServer(gram_document_id:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@DocumentUpdateActivity)
            apiService.getDocumentDetails(gram_document_id).enqueue(object :
                Callback<MainDocsModel> {
                override fun onResponse(
                    call: Call<MainDocsModel>,
                    response: Response<MainDocsModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.data.isNullOrEmpty()) {

                            if(response.body()?.status.equals("true")){
                                val list=response.body()?.data
                                Log.d("mytag",""+ Gson().toJson(response.body()));
                                documentId=list?.get(0)?.id.toString()
                                userDistrictName=list?.get(0)?.district_name.toString()
                                userTalukaName=list?.get(0)?.taluka_name.toString()
                                userVillageName=list?.get(0)?.village_name.toString()
                                userDocumentTypeName=list?.get(0)?.document_type_name.toString()
                                binding.tvDocumentName.text=list?.get(0)?.document_name
                                binding.tvDocumentDate.text= list?.get(0)?.updated_at?.let {
                                    formatDate(
                                        it
                                    )
                                }
                                binding.tvDocumentType.text=list?.get(0)?.document_type_name
                                val address="${list?.get(0)?.district_name}->${list?.get(0)?.taluka_name}->${list?.get(0)?.village_name}"
                                binding.tvAddress.text=address
                                pdfUrl= list?.get(0)?.document_pdf.toString()
                                prevFileName= list?.get(0)?.document_name.toString()
                                if(!list?.get(0)?.history_details.isNullOrEmpty())
                                {
                                    historyList= list?.get(0)?.history_details as ArrayList<HistoryDetailsItem>
                                    var adapter = RegistrationStatusHistoryAdapter(historyList)
                                    binding.recyclerViewHistory.adapter=adapter
                                    adapter.notifyDataSetChanged()
                                    binding.tvHistory.visibility=View.VISIBLE
                                    binding.recyclerViewHistory.visibility=View.VISIBLE
                                }else{
                                    binding.tvHistory.visibility=View.GONE
                                    binding.recyclerViewHistory.visibility=View.GONE

                                }
                            }else{
                                Toast.makeText(this@DocumentUpdateActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                            }

                        }else {
                            Toast.makeText(this@DocumentUpdateActivity,response.body()?.message,Toast.LENGTH_LONG).show()

                        }
                    } else{
                        binding.layoutMain.visibility=View.GONE
                        Toast.makeText(this@DocumentUpdateActivity,resources.getString(R.string.response_unsuccessfull),Toast.LENGTH_LONG).show()

                    }
                }
                override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@DocumentUpdateActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            e.printStackTrace()
        }catch (t:Throwable){

        }
    }

    private fun uploadDocumentToServer() {


    }
    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
    private fun launchScanner() {
        scanner.getStartScanIntent(this@DocumentUpdateActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Log.d("mytag", "onFailure : " + it.message)
            }
    }
    private fun convertTimeToCustomString(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return dateFormat.format(calendar.time)
    }
    private fun savePdfFileToStorage(uri: Uri?, pageCount: String, timeInMillis: String) {
        Log.d("mytag","savePdfFileToStorage : Inside")
        var myDialog=CustomProgressDialog(this@DocumentUpdateActivity)
            myDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documentName = "MH_${removeSpaces(userDistrictName.trim())}_${removeSpaces(userTalukaName.trim())}_${removeSpaces(userVillageName.trim())}_${removeSpaces(userDocumentTypeName.trim())}__${timeInMillis.trim()}.pdf"
                fileName=documentName;
                val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
                if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                    Log.e("Error", "Directory not created")
                }
                val myFile = File(mediaStorageDir, "$documentName")
                val fileOutputStream = FileOutputStream(myFile)
                val inputStream = contentResolver.openInputStream(uri!!)
                val currentDateTime = Date()
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                val formattedDateTime = formatter.format(currentDateTime)
                // Log the URI of the saved file
                val savedFileUri = Uri.fromFile(myFile)
                val reader = PdfReader(inputStream)
                val writer = PdfWriter(fileOutputStream)
                val pdfDoc = PdfDocument(reader, writer)
                val pageSize = pdfDoc.getFirstPage().getPageSize()
                for (pageNum in 1..pdfDoc.numberOfPages)
                {
                    Log.d("mytag","assigning latlong : Inside")
                    val page = pdfDoc.getPage(pageNum)
                    val document = com.itextpdf.layout.Document(pdfDoc, PageSize.A4)
                    val paragraph = Paragraph("$latitude,$longitude \n $addressFromLatLong \n $formattedDateTime")
                        .setFontSize(11f)
                        .setFontColor(ColorConstants.RED)
                        .setTextAlignment(TextAlignment.LEFT)
                    val bottomMargin = 50f // Adjust this value as needed
                    val yPos = bottomMargin
                    document.showTextAligned(
                        paragraph,
                        50f,
                        yPos,
                        pageNum,
                        TextAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        0f
                    )
                }
                val title = Paragraph("Document Identification Code")
                    .setTextAlignment(TextAlignment.CENTER) // Adjust alignment as needed
                    .setBold()
                    .setFontSize(30f) // Adjust font size as needed
                val scanInfo = Paragraph("Scan QR Code To View Document")
                    .setTextAlignment(TextAlignment.CENTER) // Adjust alignment as needed
                    .setBold()
                    .setFontSize(18f)
                val imageQr=
                    Image(ImageDataFactory.create(generateQRCodeByteArray("$documentName",500,500)))
                pdfDoc.addNewPage(1, PageSize.A4)
                val resultDocument=com.itextpdf.layout.Document(pdfDoc)
                resultDocument.add(title)
                resultDocument.add(imageQr)
                resultDocument.add(scanInfo)
                pdfDoc.close()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
                // Close the InputStream and FileOutputStream
                inputStream.close()
                fileOutputStream.close()
                documentUri=savedFileUri.toString()
                withContext(Dispatchers.Main){
                    Log.d("mytag","savePdfFileToStorage : withContext")
                    binding.btnSubmit.visibility=View.VISIBLE
                    binding.ivDocumentThumb.setImageBitmap(generateThumbnailFromPDF(documentUri.toString(),this@DocumentUpdateActivity))
                    binding.layoutDocThumb.visibility=View.VISIBLE
                    myDialog.dismiss()
                    binding.ivDocumentThumb.setOnClickListener {
                        val file=File(Uri.parse(documentUri.toString()).path)
                        openPdfFromUri(this@DocumentUpdateActivity,file)
                        Log.d("mytag","savePdfFileToStorage : openPdfFromUri")
                    }
                }
                Log.d("mytag","savePdfFileToStorage : dismiss")
            } catch (e: Exception) {
                myDialog.dismiss()
                e.printStackTrace()
                Log.d("mytag","SavePdfException : "+e.message)
            }
        }
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
        } finally {
            try {
                pdfFileDescriptor?.close()
            } catch (e: Exception) {
                Log.d("mytag","Exception : generateThumbnailFromPDF "+e.message)
                e.printStackTrace()
            }
        }
        return null

    }
    private fun generateQRCodeByteArray(
        text: String,
        width: Int,
        height: Int
    ): ByteArray? {
        try {
            val hints: MutableMap<EncodeHintType, Any> = Hashtable()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            // Use ZXing QRCodeWriter to generate BitMatrix
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints)

            // Create Bitmap from BitMatrix
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            // Convert Bitmap to byte array
            val outputStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    fun removeSpaces(inputString: String): String {
        return inputString.replace(" ", "")
    }
    private fun checkAndPromptGps() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, prompt the user to enable it
            AlertDialog.Builder(this)
                .setMessage(" Please enable GPS on your device")
                .setPositiveButton("Yes") { _, _ ->
                    // Open the location settings to enable GPS
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()
        }
    }
    private fun getAddressFromLatLong():String{
        val geocoder: Geocoder
        val addresses: List<Address>?
        geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(
            latitude, longitude,
            1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        var fullAddress=""
        if (addresses != null) {
            if(addresses.size>0){
                fullAddress= addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                val city: String = addresses!![0].locality
                val state: String = addresses!![0].adminArea
                val country: String = addresses!![0].countryName
                val postalCode: String = addresses!![0].postalCode
                val knownName: String = addresses!![0].featureName

                Log.d("mytag",fullAddress)
                Log.d("mytag",city)
                Log.d("mytag",state)
                Log.d("mytag",country)
                Log.d("mytag",postalCode)
                Log.d("mytag",knownName)
            }
        }
        return fullAddress

    }
    private fun getTheLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestThePermissions()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    latitude=it.latitude
                    longitude=it.longitude
                    addressFromLatLong=getAddressFromLatLong()
                } ?: run {
                    Toast.makeText(
                        this@DocumentUpdateActivity,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    private fun requestThePermissions() {

        PermissionX.init(this@DocumentUpdateActivity)
            .permissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "Core fundamental are based on these permissions",
                    "OK",
                    "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    getTheLocation()
                } else {
                    Toast.makeText(
                        this,
                        "These permissions are denied: $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun uploadDocuments() {
        val dialog=CustomProgressDialog(this@DocumentUpdateActivity)
        dialog.show()
        val apiService = ApiClient.create(this@DocumentUpdateActivity)
        CoroutineScope(Dispatchers.IO).launch {

            try {
                    val filePart = createFilePart(FileInfo("document_pdf", documentUri))
                    val response=apiService.uploadNewDocumentByGramsevak(
                        id =documentId,
                        file = filePart!!,
                        longitude = longitude.toString(),
                        latitude = latitude.toString(),
                        document_name = fileName
                    )
                    if(response.isSuccessful){
                        Log.d("mytag",""+response.body()?.message)
                        Log.d("mytag",""+response.body()?.status)
                        if(response.body()?.status.equals("true")){
                            runOnUiThread {
                                Toast.makeText(this@DocumentUpdateActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                            }
                            finish()
                        }else{
                            runOnUiThread {
                                Toast.makeText(this@DocumentUpdateActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                            }

                        }
                    }else{
                        runOnUiThread {
                            Toast.makeText(this@DocumentUpdateActivity,resources.getString(R.string.response_unsuccessfull),Toast.LENGTH_LONG).show()
                        }
                    }

                dialog.dismiss()

            } catch (e: Exception) {
                dialog.dismiss()
                Log.d("mytag","Upload Document Online Exception "+e.message)
            }
        }

    }
    private suspend fun createFilePart(fileInfo: FileInfo): MultipartBody.Part? {
        //val file: File? = uriToFile(applicationContext, fileInfo.fileUri)
        try {
            val file=fileInfo.fileUri.toFile()
            return file?.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData(fileInfo.fileName, it.name, requestFile)
            }
        } catch (e: Exception) {
            Log.d("mytag","SyncLandDocumentsActivity::createFilePart() Exception "+e.message)
            e.printStackTrace()
            return null

        }
    }
    private fun String.toFile(): File? {
        val uri = Uri.parse(this)
        return uri.toFile()
    }
}