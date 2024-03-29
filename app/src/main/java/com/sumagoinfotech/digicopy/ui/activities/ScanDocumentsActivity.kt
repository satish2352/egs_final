package com.sumagoinfotech.digicopy.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentDao
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentPagesBinding
import com.sumagoinfotech.digicopy.interfaces.UpdateDocumentTypeListener
import com.sumagoinfotech.digicopy.adapters.DocumentPagesAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.layout.element.Image
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDropDownDao
import com.sumagoinfotech.digicopy.database.entity.DocumentTypeDropDown
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import java.util.Hashtable

class ScanDocumentsActivity : AppCompatActivity(), UpdateDocumentTypeListener {
    private lateinit var binding: ActivityDocumentPagesBinding
    private lateinit var actDocumentType: AutoCompleteTextView
    private lateinit var ivAddDocument: ImageView
    private lateinit var etDocumentName: EditText
    private lateinit var documentName: String
    private lateinit var database: AppDatabase
    private lateinit var documentDao: DocumentDao
    private lateinit var documentTypeDao: DocumentTypeDropDownDao
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var scanner: GmsDocumentScanner
    private lateinit var documentList: List<Document>
    private lateinit var adapter: DocumentPagesAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var latitude:Double=0.0
    private  var longitude:Double=0.0
    private  var addressFromLatLong:String=""
    private  var isInternetAvailable=false
    private lateinit var dialog:CustomProgressDialog
    private var selectedDocumentId=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_pages)
        binding = ActivityDocumentPagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.upload_document)
        val layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.recyclerViewDocumentPages.layoutManager = layoutManager
        documentList = ArrayList()
        dialog=CustomProgressDialog(this)
        adapter = DocumentPagesAdapter(documentList, this)
        binding.recyclerViewDocumentPages.adapter = adapter
        documentName = ""
        database = AppDatabase.getDatabase(this)
        documentDao = database.documentDao()
        documentTypeDao = database.documentDropDownDao()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getTheLocation()
        binding.fabAddDocument.setOnClickListener {
            showDialog()
        }
        ReactiveNetwork
            .observeNetworkConnectivity(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ connectivity: Connectivity ->
                Log.d("##", "=>" + connectivity.state())
                if (connectivity.state().toString() == "CONNECTED") {
                    isInternetAvailable = true
                } else {
                    isInternetAvailable = false
                }
            }) { throwable: Throwable? -> }
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(20)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
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
        requestThePermissions()
        binding.cardOfflineDocs.setOnClickListener {

            val intent=Intent(this@ScanDocumentsActivity,SyncLandDocumentsActivity::class.java)
            startActivity(intent)
        }
        binding.cardUploadedDocs.setOnClickListener {

            val intent=Intent(this@ScanDocumentsActivity,ViewUploadedDocumentsActivity::class.java)
            startActivity(intent)
        }

    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        return try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            outputStream.close()
        }
    }

    fun fileToByteArray(file: File): ByteArray? {
        var fis: FileInputStream? = null
        return try {
            fis = FileInputStream(file)
            val byteArray = ByteArray(file.length().toInt())
            fis.read(byteArray)
            byteArray
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            fis?.close()
        }
    }

    suspend fun uriToFileByGlide(context: Context, uri: String): File? {
        return try {
            Glide.with(context)
                .asFile()
                .load(uri)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .submit()
                .get()
        } catch (e: Exception) {
            Log.d("mytag", "uriToFile : " + e.message)
            e.printStackTrace()
            null
        }
    }

    private fun requestThePermissions() {

        PermissionX.init(this@ScanDocumentsActivity)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog() {
        val dialog = Dialog(this@ScanDocumentsActivity)
        dialog.setContentView(R.layout.layout_dialog_select_document_type)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()
        actDocumentType = dialog.findViewById<AutoCompleteTextView>(R.id.actDocumentType)
        ivAddDocument = dialog.findViewById<ImageView>(R.id.ivAddDocument)
        etDocumentName = dialog.findViewById<EditText>(R.id.etDocumentName)
        var documentTypeList: List<DocumentTypeDropDown> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            documentTypeList = documentTypeDao.getDocuments()
            Log.d("mytag", "=>" + documentTypeList.size)
            adapter = DocumentPagesAdapter(documentList, this@ScanDocumentsActivity)
            adapter.notifyDataSetChanged()

            var documentNamesList = mutableListOf<String>()

            for (i in documentTypeList.indices) {
                documentNamesList.add(documentTypeList[i].documenttype)
            }
            withContext(Dispatchers.Main) {
                // Add the fetched data to the list
                val documentAdapter = ArrayAdapter(
                    this@ScanDocumentsActivity,
                    android.R.layout.simple_list_item_1,
                    documentNamesList
                )
                actDocumentType.setAdapter(documentAdapter)
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        actDocumentType.setOnFocusChangeListener { abaad, asd ->
            actDocumentType.showDropDown()
        }

        actDocumentType.setOnClickListener {
            actDocumentType.showDropDown()
        }

        actDocumentType.setOnItemClickListener { parent, view, position, id ->
            selectedDocumentId=documentTypeList[position].id.toString()
        }

        ivAddDocument.setOnClickListener {

            if (validateFields()) {
                val calendar = Calendar.getInstance()
                val timeInMillis = convertTimeToCustomString(calendar.timeInMillis);
                if (etDocumentName.text.length > 0 && !etDocumentName.text.isNullOrEmpty()) {
                    documentName =
                        "${actDocumentType.text.toString()}_${etDocumentName.text.toString()}_${timeInMillis}"
                } else {
                    documentName =
                        "${actDocumentType.text.toString()}_${timeInMillis}"
                }
                Log.d("mytag", "Document Name >$documentName")
                launchScanner()
                dialog.dismiss()
            } else {

            }
        }
    }

    private fun validateFields(): Boolean {
        val validationResults = mutableListOf<Boolean>()
        // Village
        if (actDocumentType.enoughToFilter()) {
            actDocumentType.error = null
            validationResults.add(true)
        } else {
            actDocumentType.error = resources.getString(R.string.select_document)
            validationResults.add(false)
        }

        return !validationResults.contains(false)
    }

    private fun launchScanner() {
        scanner.getStartScanIntent(this@ScanDocumentsActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Log.d("mytag", "onFailure : " + it.message)
            }
    }

    private fun saveRecordToDatabase(
        pdfUri: Uri,
        documentName: String,
        pageCount: String,
        documentId: String,
        date: String
    ) {
        Log.d("mytag", "------>" + documentName)
        val document = Document(
            documentName = documentName,
            pageCount = pageCount,
            documentUri = pdfUri.toString(),
            isSynced = false,
            documentId = documentId,
            date=date,
            latitude = latitude.toString(),
            longitude =longitude.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rows = documentDao.insertDocument(document)
                if (rows > 0) {
                    runOnUiThread {
                        val toast = Toast.makeText(
                            this@ScanDocumentsActivity,
                            "Document added successfully",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                        Log.d("mytag", "Document added successfully : $rows")
                        setCount()
                    }
                } else {
                    runOnUiThread {
                        val toast = Toast.makeText(
                            this@ScanDocumentsActivity,
                            "Document not added please try again",
                            Toast.LENGTH_SHORT
                        )
                        setCount()
                        toast.show()
                        Log.d("mytag", "Document not added please try again : $rows")
                    }
                }
                Log.d("mytag", "Document Inserted : $rows")
            } catch (e: Exception) {
                Log.d("mytag", "Exception saveRecordToDatabase : ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    val toast = Toast.makeText(
                        this@ScanDocumentsActivity,
                        "Document not added please try again",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
    }
    private fun savePdfFileToStorage(uri: Uri?, pageCount: String, documentId: String) {
        Log.d("mytag","savePdfFileToStorage : Inside")
        dialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
                if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                    Log.e("Error", "Directory not created")
                }
                val myFile = File(mediaStorageDir, "$documentName.pdf")
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
                        .setFontSize(18f)
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
                val imageQr=Image(ImageDataFactory.create(generateQRCodeByteArray("$documentName.pdf",500,500)))
                pdfDoc.addNewPage(1, PageSize.A4)
                val resultDocument=com.itextpdf.layout.Document(pdfDoc)
                resultDocument.add(imageQr)
                pdfDoc.close()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
                // Close the InputStream and FileOutputStream
                inputStream.close()
                fileOutputStream.close()
                saveRecordToDatabase(savedFileUri, documentName, pageCount, documentId = selectedDocumentId,formattedDateTime)
                dialog.dismiss()
            } catch (e: Exception) {
                dialog.dismiss()
                e.printStackTrace()
                Log.d("mytag","SavePdfException : "+e.message)
            }
        }
    }


    private fun convertTimeToCustomString(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return dateFormat.format(calendar.time)
    }

    override fun onResume() {
        super.onResume()
        //updateDocumentList()
        checkAndPromptGps()
        setCount()
    }

    override fun onPause() {
        super.onPause()
        Log.d("mytag", "onPause=>" + documentList.size)
    }

    override fun onStop() {
        super.onStop()
        Log.d("mytag", "onStop=>" + documentList.size)
    }

    override fun onRestart() {
        super.onRestart()

    }

    override fun onPostResume() {
        super.onPostResume()
        setCount()
    }


    private fun updateDocumentList() {
        CoroutineScope(Dispatchers.IO).launch {
            documentList = documentDao.getAllDocuments()
            Log.d("mytag", "=>" + documentList.size)
            adapter = DocumentPagesAdapter(documentList, this@ScanDocumentsActivity)
            withContext(Dispatchers.Main) {
                // Add the fetched data to the list
                adapter = DocumentPagesAdapter(documentList, this@ScanDocumentsActivity)
                binding.recyclerViewDocumentPages.adapter = adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
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
                        this@ScanDocumentsActivity,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    suspend fun uriStringToBitmap(
        context: Context,
        uriString: String,
        text: String,
        addressText: String
    ): Uri? {
        Log.d("mytag", "uriStringToBitmap=>")
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .submit()
                val bitmap = futureTarget.get()

                // Add text overlay to the bitmap
                val canvas = Canvas(bitmap)
                val paint = Paint().apply {
                    color = Color.RED
                    textSize = 50f // Text size in pixels
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                val x = 50f // Adjust the x-coordinate as needed
                val y = bitmap.height.toFloat() - 50f // Adjust the y-coordinate as needed
                val xAddress = 50f // Adjust the x-coordinate as needed
                val yAddress = bitmap.height.toFloat() - 100f
                canvas.drawText(text, x, y, paint)
                canvas.drawText(addressText, xAddress, yAddress, paint)
                // Save the modified bitmap back to the same location
                saveBitmapToFile(context, bitmap, uri)
                uri // Return the URI of the modified bitmap
            } catch (e: Exception) {
                Log.d("mytag", "uriStringToBitmap => " + e.message)
                e.printStackTrace()
                null
            }
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            outputStream?.flush()
            outputStream?.close()
        } catch (e: Exception) {
            Log.d("mytag", "saveBitmapToFile => " + e.message)
            e.printStackTrace()
        }
    }

    suspend fun uriToBitmapByGlide(context: Context, uri: Uri): Bitmap? {
        return try {
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .submit()
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
    private fun generateQRCodeBitmap(
        text: String,
        width: Int,
        height: Int
    ): Bitmap? {
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

            return bmp
        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun onUpdateDocumentType(documentName: Document) {
    }

    fun setCount(){
        CoroutineScope(Dispatchers.IO).launch{
            val documentCount=documentDao.getDocumentsCount();
            withContext(Dispatchers.Main) {
                binding.tvDocumentsCount.setText("${documentCount}")
            }
        }
    }
}

