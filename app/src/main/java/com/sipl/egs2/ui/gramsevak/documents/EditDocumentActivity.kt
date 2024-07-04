package com.sipl.egs2.ui.gramsevak.documents

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import com.permissionx.guolindev.PermissionX
import com.sipl.egs2.R
import com.sipl.egs2.adapters.PdfPageAdapter
import com.sipl.egs2.databinding.ActivityEditDocumentBinding
import com.sipl.egs2.ui.gramsevak.ReportsActivity
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.webservice.ApiClient
import io.getstream.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditDocumentActivity : AppCompatActivity(),PdfPageAdapter.OnDeletePageListener,PdfPageAdapter.OnPdfPageClickListener {
    private lateinit var binding:ActivityEditDocumentBinding
    private lateinit var pdfPageAdapter: PdfPageAdapter
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var scanner: GmsDocumentScanner
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var documentId=""
    private var pdfUrl=""
    var tempPdfFile : File? = null
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private  var addressFromLatLong:String=""
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    private var fileName=""
    private var documentType=""
    private var fileSize:Long=0
    private lateinit var dialog: CustomProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title=resources.getString(R.string.edit_document)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pdfUrl=intent.getStringExtra("url")!!;
        documentId=intent.getStringExtra("document_id")!!;
        fileName=intent.getStringExtra("fileName")!!;
        documentType=intent.getStringExtra("documentType")!!;
        dialog= CustomProgressDialog(this)
        dialog.show()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        noInternetDialog= NoInternetDialog(this)
        ReactiveNetwork
            .observeNetworkConnectivity(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectivity: Connectivity ->
                Log.d("##", "=>" + connectivity.state())
                if (connectivity.state().toString() == "CONNECTED") {
                    isInternetAvailable = true
                    noInternetDialog.hideDialog()
                } else {
                    isInternetAvailable = false
                    noInternetDialog.showDialog()
                }
            }) { throwable: Throwable? -> }
        if (!isLocationEnabled()) {
            showEnableLocationDialog()
        } else {
            requestLocationUpdates()
        }
        downloadAndDisplayPdf()
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
                GmsDocumentScannerOptions.CAPTURE_MODE_MANUAL
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        scanner = GmsDocumentScanning.getClient(options)
        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val result = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        result?.getPages()?.let { pages ->
                            val emptyByteArray = ByteArray(0)
                            val newPdfFile=createTempPdfFile(this@EditDocumentActivity,emptyByteArray)
                            result?.getPdf()?.let { pdf ->
                                val pdfUri = pdf.uri
                                mergePdfWithExisting(this@EditDocumentActivity,pdfUri,tempPdfFile!!,newPdfFile!!)
                                tempPdfFile=newPdfFile;
                                withContext(Dispatchers.Main){
                                    tempPdfFile?.let {
                                        pdfPageAdapter = PdfPageAdapter(this@EditDocumentActivity, it, this@EditDocumentActivity,this@EditDocumentActivity)
                                        binding.recyclerView.adapter = pdfPageAdapter
                                        pdfPageAdapter.notifyDataSetChanged()
                                        tempPdfFile=newPdfFile;
                                        saveFileToStorage(tempPdfFile!!)
                                    }
                                }
                            }

                        }
                    }

                }
            }
        binding.fabAdd.setOnClickListener {
            if(isInternetAvailable){
                requestThePermissions()
                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                } else {
                    requestLocationUpdates()
                }
                launchScanner()
            }else{noInternetDialog.showDialog()}

        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val builder = AlertDialog.Builder(this@EditDocumentActivity)
                builder.setTitle("Exit")
                    .setMessage("Are you sure you want to exit this screen?")
                    .setPositiveButton("Yes") { _, _ ->
                        // If "Yes" is clicked, exit the app
                        finish()
                    }
                    .setNegativeButton(resources.getString(R.string.no),null)  // If "No" is clicked, do nothing
                    .show()

            }
        })
        binding.btnUploadDocument.setOnClickListener {

            val itemCount = pdfPageAdapter?.itemCount ?: 0
            if(isInternetAvailable){
              if(tempPdfFile!=null ){
                  if(fileSize!= tempPdfFile!!.length() && itemCount>1)
                  {
                      uploadDocuments()
                  }else{
                      Toast.makeText(this@EditDocumentActivity,
                          getString(R.string.please_make_changes_to_document_before_submitting),Toast.LENGTH_LONG).show()
                  }
              }else{
                  Toast.makeText(this@EditDocumentActivity,
                      getString(R.string.please_try_again),Toast.LENGTH_LONG).show()
              }
          }else{
              noInternetDialog.showDialog()
          }

        }
        binding.tvDocumentType.setText(documentType)


    }
    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this@EditDocumentActivity)
        builder.setMessage("Location services are disabled. App requires location for core features please enable gps & location.?")
            .setCancelable(false).setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                // Handle the case when the user refuses to enable location services
                Toast.makeText(
                    this@EditDocumentActivity,
                    "Unable to retrieve location without enabling location services",
                    Toast.LENGTH_LONG
                ).show()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun downloadAndDisplayPdf(){
        CoroutineScope(Dispatchers.IO).launch {
            val byteArray = downloadPdfAsByteArrayRetro(pdfUrl!!)
            if (byteArray != null) {
                showUi()
                tempPdfFile = createTempPdfFile(this@EditDocumentActivity, byteArray)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    if (tempPdfFile != null) {

                        fileSize= tempPdfFile!!.length()
                        // Now you can pass this tempPdfFile to the adapter
                        pdfPageAdapter = PdfPageAdapter(this@EditDocumentActivity, tempPdfFile!!,this@EditDocumentActivity,this@EditDocumentActivity)
                        binding.recyclerView.layoutManager = GridLayoutManager(
                            this@EditDocumentActivity,
                            2
                        ) // Adjust number of columns as needed
                        binding.recyclerView.adapter = pdfPageAdapter
                        tempPdfFile?.let { it1 -> saveFileToStorage(it1) };
                    }
                }
            }else{
                hideUi()
                runOnUiThread {
                    Toast.makeText(this@EditDocumentActivity,resources.getString(R.string.pdf_download_failed_try_again),Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.btnRetry.setOnClickListener {
            downloadAndDisplayPdf()
        }
    }
    override fun onResume() {
        super.onResume()
        checkAndPromptGps()
        getTheLocation()
        requestThePermissions()
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this@EditDocumentActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@EditDocumentActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            ActivityCompat.requestPermissions(
                this@EditDocumentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
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
                    this@EditDocumentActivity, "Unable to retrieve location", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            val builder = AlertDialog.Builder(this@EditDocumentActivity)
            builder.setTitle("Exit")
                .setMessage("Are you sure you want to exit this screen?")
                .setPositiveButton("Yes") { _, _ ->
                    // If "Yes" is clicked, exit the app
                    finish()
                }
                .setNegativeButton(resources.getString(R.string.no),null)  // If "No" is clicked, do nothing
                .show()
        }

        return super.onOptionsItemSelected(item)

    }
    private fun launchScanner() {
        scanner.getStartScanIntent(this@EditDocumentActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Log.d("mytag", "onFailure : " + it.message)
            }
    }
    private suspend fun downloadPdfAsByteArrayRetro(urlStr: String): ByteArray? = withContext(Dispatchers.IO) {
        var byteArray: ByteArray? = null
        try {
            val service = ApiClient.create(this@EditDocumentActivity)
            val response = service.downloadPdf(urlStr)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                   showUi()
                    val inputStream = responseBody.byteStream()
                    val outputStream = ByteArrayOutputStream()

                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }

                    outputStream.flush()
                    byteArray = outputStream.toByteArray()

                    inputStream.close()
                    outputStream.close()
                } else {
                   hideUi()
                    runOnUiThread {  Toast.makeText(this@EditDocumentActivity,resources.getString(R.string.please_try_again),Toast.LENGTH_LONG).show() }
                    Log.e("mytag", "Response body is null")
                }
            } else {
                hideUi()
                runOnUiThread {  Toast.makeText(this@EditDocumentActivity,resources.getString(R.string.please_try_again),Toast.LENGTH_LONG).show() }
                Log.e("mytag", "Response unsuccessful: ${response.code()}")
            }
        } catch (e: Exception) {
            hideUi()
            e.printStackTrace()
           runOnUiThread {  Toast.makeText(this@EditDocumentActivity,resources.getString(R.string.please_try_again),Toast.LENGTH_LONG).show() }
            Log.e("mytag", "Exception: ${e.message}")
        }
        byteArray
    }
    private fun showUi(){
            binding.layoutOne.visibility=View.VISIBLE
            binding.fabAdd.visibility=View.VISIBLE
        binding.btnRetry.visibility=View.GONE
    }
    private fun hideUi(){
        binding.layoutOne.visibility=View.GONE
        binding.fabAdd.visibility=View.GONE
        binding.btnRetry.visibility=View.VISIBLE
    }

    suspend fun createTempPdfFile(context: Context, byteArray: ByteArray): File? =
        withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                // Create a temporary file
                tempFile = File.createTempFile("temp_pdf", ".pdf", context.cacheDir)

                // Write the byte array to the temporary file
                val outputStream = FileOutputStream(tempFile)
                outputStream.write(byteArray)
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("mytag","Exception "+e.message)
            }
            tempFile
        }

    suspend fun saveFileToStorage(inputFile: File) {
        val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.e("Error", "Directory not created")
            return // Exit if directory creation fails
        }

        val outputFile = File(mediaStorageDir, "inputFile.name.pdf")

        try {
            val inputStream = FileInputStream(inputFile)
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // Close the streams
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun deletePageFromPdf(inputPdfFile: File, outputPdfFile: File, pageIndex: Int) {
        try {
            // Open the input PDF
            val pdfReader = PdfReader(inputPdfFile)
            val pdfDocument = PdfDocument(pdfReader)

            // Create a new PDF writer
            val pdfWriter = PdfWriter(FileOutputStream(outputPdfFile))

            // Create a new PDF document
            val newPdfDocument = PdfDocument(pdfWriter)

            // Copy pages from the original PDF to the new PDF, excluding the specified page
            for (i in 1..pdfDocument.numberOfPages) {
                if (i != pageIndex + 1) { // Page numbers start from 1
                    val page = pdfDocument.getPage(i)
                    newPdfDocument.addPage(page.copyTo(newPdfDocument))
                }
            }

            // Close the documents
            pdfDocument.close()
            newPdfDocument.close()
        } catch (e: IOException) {
            Log.d("mytag","Exception "+e.message)
            e.printStackTrace()
        }
    }



    override fun onDeletePage(pageIndex: Int)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val emptyByteArray = ByteArray(0)
            val newPdfFile=createTempPdfFile(this@EditDocumentActivity,emptyByteArray)
            tempPdfFile?.let { deletePageFromPdf(it,newPdfFile!!, pageIndex) }

            // If deletion was successful, update the RecyclerView
            withContext(Dispatchers.Main){
                newPdfFile?.let {
                    pdfPageAdapter = PdfPageAdapter(this@EditDocumentActivity, it, this@EditDocumentActivity,this@EditDocumentActivity)
                    binding.recyclerView.adapter = pdfPageAdapter
                    pdfPageAdapter.notifyDataSetChanged()
                    tempPdfFile=newPdfFile;
                }
            }
        }

    }


    private fun scaleBitmap(bitmap: Bitmap, pageSize: PageSize): Bitmap {
        val maxWidth = pageSize.width - 100
        val maxHeight = pageSize.height - 100
        val width = bitmap.width
        val height = bitmap.height
        val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        return Bitmap.createScaledBitmap(bitmap, (width * scaleFactor).toInt(), (height * scaleFactor).toInt(), true)
    }

    private fun scaledBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    suspend fun mergePdfWithExisting(context: Context, pdfUris: Uri, existingPdfFile: File, outputFile: File) {

        runOnUiThread {
            dialog.show()
        }
        withContext(Dispatchers.IO) {
            val writerProperties = WriterProperties()
            writerProperties.setCompressionLevel(9)
            writerProperties.setFullCompressionMode(true)
            val writer = PdfWriter(FileOutputStream(outputFile),writerProperties)
            val pdfDocument = PdfDocument(writer)
            // Open the existing PDF file
            val existingPdfReader = PdfReader(existingPdfFile)
            val existingPdfDocument = PdfDocument(existingPdfReader)
            val startPageNum = existingPdfDocument.numberOfPages
            val currentDateTime = Date()
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            val formattedDateTime = formatter.format(currentDateTime)
            try {

                // Merge existing PDF document into the output document
                existingPdfDocument.copyPagesTo(1, existingPdfDocument.numberOfPages, pdfDocument)
                // Loop through each PDF URI
                // Open the PDF file from URI
                val pdfReader = PdfReader(context.contentResolver.openInputStream(pdfUris))
                val pdfDocumentToAdd = PdfDocument(pdfReader)
                pdfDocumentToAdd.copyPagesTo(1, pdfDocumentToAdd.numberOfPages, pdfDocument)

                val endPageNum = pdfDocument.numberOfPages
                for (i in startPageNum + 1..endPageNum) {
                    Log.d("mytag","------------>"+i)
                    val page = pdfDocument.getPage(i)
                    val document = Document(page.document, PageSize.A4)
                    val paragraph = Paragraph("$latitude,$longitude \n" + " $addressFromLatLong \n" + " $formattedDateTime")
                        .setFontSize(11f)
                        .setFontColor(ColorConstants.RED)
                        .setTextAlignment(TextAlignment.LEFT)
                    val bottomMargin = 50f // Adjust this value as needed
                    val yPos = bottomMargin
                    document.showTextAligned(
                        paragraph,
                        50f,
                        yPos,
                        i,
                        TextAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        0f
                    )
                }
                // Close the input PDF document
                pdfDocumentToAdd.close()
            } catch (e: Exception) {
                runOnUiThread {
                    dialog.dismiss()
                }
                Log.d("mytag","Exception "+e.message)
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    dialog.dismiss()
                }
                // Close all documents and writer
                pdfDocument.close()
                existingPdfDocument.close()
                writer.close()
                existingPdfReader.close()
            }
        }
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
                .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                    // Handle the case when the user chooses not to enable GPS
                }
                .show()
        }
    }
    private fun getAddressFromLatLong():String{
        val geocoder: Geocoder
        try {
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
        } catch (e: Exception) {
            return ""
        }

    }
    private fun getTheLocation() {

        try {
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
                            this@EditDocumentActivity,
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {

        }
    }
    private fun requestThePermissions() {

        try {
            PermissionX.init(this@EditDocumentActivity)
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
        } catch (e: Exception) {
        }
    }

    private fun uploadDocuments() {
        val dialog= CustomProgressDialog(this@EditDocumentActivity)
        dialog.show()
        val apiService = ApiClient.create(this@EditDocumentActivity)
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val filePart = createFilePart(tempPdfFile!!)
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
                            Toast.makeText(this@EditDocumentActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                        }

                        val intent = Intent(this@EditDocumentActivity, ReportsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }else{
                        runOnUiThread {
                            Toast.makeText(this@EditDocumentActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                        }

                    }
                }else{
                    runOnUiThread {
                        Toast.makeText(this@EditDocumentActivity,resources.getString(R.string.response_unsuccessfull),Toast.LENGTH_LONG).show()
                    }
                }

                dialog.dismiss()

            } catch (e: Exception) {
                dialog.dismiss()
                Log.d("mytag","Upload Document Online Exception "+e.message)
            }
        }

    }
    private suspend fun createFilePart(file: File): MultipartBody.Part? {
        //val file: File? = uriToFile(applicationContext, fileInfo.fileUri)
        try {
            //val file=fileInfo.fileUri.toFile()
            return file?.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData("document_pdf", it.name, requestFile)
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
    private fun showPhotoZoomDialog(bitmap: Bitmap){

        try {
            val dialog= Dialog(this@EditDocumentActivity)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@EditDocumentActivity)
                .load(bitmap)
                .into(photoView)
            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {

        }
    }

    override fun onPdfPageClick(bitmap: Bitmap) {
        showPhotoZoomDialog(bitmap)
    }
}