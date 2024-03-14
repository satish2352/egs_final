package com.sumagoinfotech.digicopy.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
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
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import com.permissionx.guolindev.PermissionX
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.database.entity.DocumentType
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentPagesBinding
import com.sumagoinfotech.digicopy.interfaces.UpdateDocumentTypeListener
import com.sumagoinfotech.digicopy.ui.adapters.DocumentPagesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DocumentPagesActivity : AppCompatActivity(),UpdateDocumentTypeListener {
    private lateinit var binding:ActivityDocumentPagesBinding
    private lateinit var actDocumentType:AutoCompleteTextView
    private lateinit var ivAddDocument:ImageView
    private lateinit var etDocumentName:EditText
    private lateinit var documentName:String
    private lateinit var database: AppDatabase
    private lateinit var documentDao:DocumentDao
    private lateinit var documentTypeDao:DocumentTypeDao
    private lateinit var scannerLauncher:ActivityResultLauncher<IntentSenderRequest>
    private lateinit var scanner:GmsDocumentScanner
    private lateinit var documentList:List<Document>
    private lateinit var adapter:DocumentPagesAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_pages)
        binding = ActivityDocumentPagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.upload_document)
        val layoutManager= GridLayoutManager(this,2, RecyclerView.VERTICAL,false)
        binding.recyclerViewDocumentPages.layoutManager=layoutManager
        documentList=ArrayList()
        adapter= DocumentPagesAdapter(documentList,this)
        binding.recyclerViewDocumentPages.adapter=adapter
        documentName=""
        database= AppDatabase.getDatabase(this)
        documentDao=database.documentDao()
        documentTypeDao=database.documentTypeDao()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getTheLocation()

//        CoroutineScope(Dispatchers.IO).launch{
//            documentList=documentDao.getAllUsers()
//            Log.d("mytag","=>"+documentList.size)
//            adapter= DocumentPagesAdapter(documentList)
////            binding.recyclerViewSyncLabourData.adapter=adapter
////            adapter.notifyDataSetChanged()
//
//            withContext(Dispatchers.Main) {
//                // Add the fetched data to the list
//                adapter= DocumentPagesAdapter(documentList)
//                binding.recyclerViewDocumentPages.adapter=adapter
//                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
//            }
//        }
        binding.fabAddDocument.setOnClickListener {

            showDialog()
        }
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(20)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
         scanner = GmsDocumentScanning.getClient(options)
         scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                result ->
            if (result.resultCode == RESULT_OK) {

                val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
                if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                    Log.e("Error", "Directory not created")
                }
                val uri = Uri.parse(mediaStorageDir.absolutePath)
                val myAppFolder = File(uri.toString())
                if (!myAppFolder.exists()) {
                    myAppFolder.mkdirs()
                }
                val calendar = Calendar.getInstance()
                val time = convertTimeToCustomString(calendar.timeInMillis)
                val outputFile = File.createTempFile("egs", ".pdf", myAppFolder)
                val fileOutputStream = FileOutputStream(outputFile)
                val pdfDocument = PdfDocument(PdfWriter(fileOutputStream))
                val resultDocument = com.itextpdf.layout.Document(pdfDocument)
                val result = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                val job= CoroutineScope(Dispatchers.IO).launch{
                result?.getPages()?.let { pages ->
                    for (page in pages) {
                        try {
//                            val imageUri = page.imageUri
//                            val savedImageUri=saveImageToStorage(this@DocumentPagesActivity, uriToBitmap(imageUri)!!)
//                            val latestUri=uriStringToBitmap(this@DocumentPagesActivity,savedImageUri.toString(),"HEYYYY","ZZZZZZZZZZZZZZZZ")
//                            Log.d("mytag","Latest generated image uri => "+latestUri.toString())
//                            val bitmap= uriToBitmapByGlide(this@DocumentPagesActivity, latestUri!!)
//                            resultDocument.add(Image(ImageDataFactory.create(bitmapToByteArray(bitmap!!))))
                        } catch (e: Exception) {
                            Log.d("mytag","CoroutineScope : "+e.message)
                            e.printStackTrace()
                        }
                    }
                    }
                }
                result?.getPdf()?.let { pdf ->
                    val pdfUri = pdf.getUri()
                    val pageCount = pdf.getPageCount()
                    val calendar = Calendar.getInstance()
                    val timeInMillis = convertTimeToCustomString(calendar.timeInMillis);
                    saveFile1(pdfUri,pageCount.toString(),timeInMillis)
                }
               /* CoroutineScope(Dispatchers.IO).launch {
                    job.join()
                    resultDocument.close()
                }*/
            }
        }
        requestThePermissions()

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
            Log.d("mytag","uriToFile : "+e.message)
            e.printStackTrace()
            null
        }
    }
    fun createPdfWithImagesAndText(filePath: String, imageUris: List<String>, text: String) {
        // Create a new PDF document
        val writer = PdfWriter(filePath)
        val pdfDoc = PdfDocument(writer)
        val document = com.itextpdf.layout.Document(pdfDoc, PageSize.A4)

        // Add images to the document
        for (imageUri in imageUris) {
            val image = Image(ImageDataFactory.create(imageUri))
            image.scaleToFit(500f, 500f)
            document.add(image)
            document.add(Paragraph("\n")) // Add some space between images
        }

        // Add text to each page of the document
        for (i in 1..pdfDoc.numberOfPages) {
            val currentPage = pdfDoc.getPage(i)
            val canvas = PdfCanvas(currentPage)
            var x = document.leftMargin.toFloat()
            var y = document.bottomMargin.toFloat()
            val paragraph = Paragraph(text)
            paragraph.setFixedPosition(x, y, PageSize.A4.width - document.leftMargin - document.rightMargin)
            canvas.beginText().setFontAndSize(null, 12f)
                .moveText(x.toDouble(), y.toDouble())
                .showText(text)
                .endText()
        }

        // Close the document
        document.close()
    }
    private fun requestThePermissions() {

        PermissionX.init(this@DocumentPagesActivity)
            .permissions(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    getTheLocation()
                } else {
                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                }
            }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showDialog() {
        val dialog= Dialog(this@DocumentPagesActivity)
        dialog.setContentView(R.layout.layout_dialog_select_document_type)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()
        actDocumentType=dialog.findViewById<AutoCompleteTextView>(R.id.actDocumentType)
        ivAddDocument=dialog.findViewById<ImageView>(R.id.ivAddDocument)
        etDocumentName=dialog.findViewById<EditText>(R.id.etDocumentName)
        var documentTypeList:List<DocumentType> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch{
            documentTypeList=documentTypeDao.getDocuments()
            Log.d("mytag","=>"+documentTypeList.size)
            adapter= DocumentPagesAdapter(documentList,this@DocumentPagesActivity)
             adapter.notifyDataSetChanged()

            var documentNamesList= mutableListOf<String>()

            for(i in documentTypeList.indices){
                documentNamesList.add(documentTypeList[i].documentName)
         }
            withContext(Dispatchers.Main) {
                // Add the fetched data to the list
                val documentAdapter = ArrayAdapter(
                    this@DocumentPagesActivity, android.R.layout.simple_list_item_1, documentNamesList
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

        ivAddDocument.setOnClickListener {

            if(validateFields())
            {
                val calendar = Calendar.getInstance()
                val timeInMillis = convertTimeToCustomString(calendar.timeInMillis);
                if(etDocumentName.text.length>0 && !etDocumentName.text.isNullOrEmpty()) {
                    documentName =
                        "${actDocumentType.text.toString()}_${etDocumentName.text.toString()}_${timeInMillis}"
                }else{
                    documentName =
                        "${actDocumentType.text.toString()}_${timeInMillis}"
                }
                Log.d("mytag", "Document Name >$documentName")
                launchScanner()
                dialog.dismiss()
            }else{

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

    private fun  launchScanner()
    {
        scanner.getStartScanIntent(this@DocumentPagesActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Log.d("mytag","onFailure : "+it.message)
            }
    }
    private fun saveRecordToDatabase(pdfUri: Uri, documentName: String,pageCount:String,documentId:String) {
        Log.d("mytag","------>"+documentName)
        val document = Document(
            documentName=documentName,
            pageCount = pageCount,
            documentUri = pdfUri.toString(),
            isSynced = false,
            documentId = documentId)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rows=documentDao.insertDocument(document)
                val documentType = documentTypeDao.getDocumentByName(documentName)
                if (documentType != null) {
                    documentType.isAdded = true
                    documentTypeDao.updateDocumentType(documentType)
                }
                if(rows>0){
                    runOnUiThread {
                        val toast= Toast.makeText(this@DocumentPagesActivity,"Document added successfully",Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }else{
                    runOnUiThread {
                        val toast=Toast.makeText(this@DocumentPagesActivity,"Document not added please try again",Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
                documentList=documentDao.getAllUsers()
                withContext(Dispatchers.Main) {
                    adapter= DocumentPagesAdapter(documentList,this@DocumentPagesActivity)
                    binding.recyclerViewDocumentPages.adapter=adapter
                    adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
                }
                Log.d("mytag","Document Inserted : $rows")


            } catch (e: Exception) {
                Log.d("mytag","Exception Inserted : ${e.message}")
                e.printStackTrace()
            }
        }

    }
    fun saveFile1(uri: Uri?,pageCount: String,documentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
                if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                    Log.e("Error", "Directory not created")
                }
                val calendar = Calendar.getInstance()
                val customFileName = convertTimeToCustomString(calendar.timeInMillis);
                val myFile = File(mediaStorageDir, "$customFileName.pdf")
                val fileOutputStream = FileOutputStream(myFile)

                // Get the InputStream from the Uri
                val inputStream = contentResolver.openInputStream(uri!!)

                // Read content from the InputStream and write it to the FileOutputStream


                // Log the URI of the saved file
                val savedFileUri = Uri.fromFile(myFile)
                val calendar2 = Calendar.getInstance()
                val customFileName2 = convertTimeToCustomString(calendar2.timeInMillis);
                val myFile2 = File(mediaStorageDir, "$customFileName2.pdf")
                val reader = PdfReader(inputStream)
                val writer = PdfWriter(fileOutputStream)
                val pdfDoc = PdfDocument(reader, writer)
                val pageSize = pdfDoc.getFirstPage().getPageSize()
                val a4PageSize = PageSize.A4
                for (pageNum in 1..pdfDoc.numberOfPages) {
                    Log.d("mytag","here---->"+pageNum)

                    val page = pdfDoc.getPage(pageNum)
    //                val pdfCanvas = PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc)
    //
    //                val canvas = com.itextpdf.layout.Canvas(pdfCanvas, a4PageSize)
    //                canvas
    //                    .setFontSize(12f)
    //                    .showTextAligned("overlayText--------------->", pageSize.width / 2, pageSize.height - 50, TextAlignment.CENTER)
    //                    .showTextAligned("Additional Text", pageSize.width / 2, 50f, TextAlignment.CENTER)
    //                val paragraph = Paragraph("This is a paragraph of text.")
    //                canvas.add(paragraph.setFontSize(10f).setMargin(20f))
    //                val redColor = DeviceRgb(255, 0, 0)
    //                canvas.setFontColor(redColor)
    //                    .showTextAligned("Red Colored Text", a4PageSize.width / 2, a4PageSize.height / 2, TextAlignment.CENTER)

                    val document = com.itextpdf.layout.Document(pdfDoc, PageSize.A4)
                    val paragraph = Paragraph("dfgfdgdgdfgdfgdfgdfgdfgfdgfdgdfgdfgdfgdf")
                        .setFontSize(18f)
                        .setFontColor(ColorConstants.RED)
                        .setTextAlignment(TextAlignment.CENTER)
                    val bottomMargin = 50f // Adjust this value as needed
                   // val yPos = bottomMargin + paragraph.height
                    val fontSize = 18f // Adjust this value to match the font size of your paragraph
                    val lineSpacing = 1.2f // Adjust this value to match the line spacing of your paragraph

                    val yPos = bottomMargin + 50f

                    document.showTextAligned(paragraph, pageSize.width / 2, yPos, pageNum, TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0f)

                    document.showTextAligned(paragraph, pageSize.width / 2, pageSize.height / 2, pageNum, TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0f)
                }

                pdfDoc.close()

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
                // Close the InputStream and FileOutputStream
                inputStream.close()
                fileOutputStream.close()


                Log.d("mytag", "#######################=>"+savedFileUri.toString())
                saveRecordToDatabase(savedFileUri,documentName,pageCount, documentId =documentId )
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the IOException
            }
        }
    }
    fun saveImage(uri: Uri?,pageCount: String,documentId: String) {
        try {
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.e("Error", "Directory not created")
                return
            }
            val calendar = Calendar.getInstance()
            val customFileName = convertTimeToCustomString(calendar.timeInMillis);
            val myFile = File(mediaStorageDir, "$customFileName.jpeg")
            val fileOutputStream = FileOutputStream(myFile)

            // Get the InputStream from the Uri
            val inputStream = contentResolver.openInputStream(uri!!)

            // Read content from the InputStream and write it to the FileOutputStream
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                fileOutputStream.write(buffer, 0, bytesRead)
            }

            // Close the InputStream and FileOutputStream
            inputStream.close()
            fileOutputStream.close()
            // Log the URI of the saved file
            val savedFileUri = Uri.fromFile(myFile)
            Log.d("mytag", savedFileUri.toString())
            saveRecordToDatabase(savedFileUri,documentName,pageCount,documentId)
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the IOException
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
        /*CoroutineScope(Dispatchers.IO).launch{
            documentList=documentDao.getAllUsers()
            Log.d("mytag","=>"+documentList.size)
            adapter= DocumentPagesAdapter(documentList,this@DocumentPagesActivity)
            withContext(Dispatchers.Main) {
                // Add the fetched data to the list
                adapter= DocumentPagesAdapter(documentList,this@DocumentPagesActivity)
                binding.recyclerViewDocumentPages.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }*/
        updateDocumentList()
    }

    override fun onPause() {
        super.onPause()
        Log.d("mytag","onPause=>"+documentList.size)
    }

    override fun onStop() {
        super.onStop()
        Log.d("mytag","onStop=>"+documentList.size)
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onUpdateDocumentType(document: Document) {
        Log.d("mytag",""+documentName)
        CoroutineScope(Dispatchers.IO).launch{
            var documentType=documentTypeDao.getDocumentByName(documentName)
            documentType?.isAdded=false
            documentTypeDao.updateDocumentType(documentType!!)
            documentDao.deleteDocument(document)

        }
    }

    private fun updateDocumentList(){
        CoroutineScope(Dispatchers.IO).launch{
            documentList=documentDao.getAllUsers()
            Log.d("mytag","=>"+documentList.size)
            adapter= DocumentPagesAdapter(documentList,this@DocumentPagesActivity)
            withContext(Dispatchers.Main) {
                // Add the fetched data to the list
                adapter= DocumentPagesAdapter(documentList,this@DocumentPagesActivity)
                binding.recyclerViewDocumentPages.adapter=adapter
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
                    //binding.etLocation.setText("${it.latitude},${it.longitude}")
                } ?: run {
                    Toast.makeText(
                        this@DocumentPagesActivity,
                        "Unable to retrieve location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    suspend fun uriStringToBitmap(context: Context, uriString: String, text: String, addressText: String): Uri? {
        Log.d("mytag","uriStringToBitmap=>")
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
                Log.d("mytag","uriStringToBitmap => "+e.message)
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
            Log.d("mytag","saveBitmapToFile => "+e.message)
            e.printStackTrace()
        }
    }

    fun saveImageToStorage(context: Context, bitmap: Bitmap): Uri? {

        val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.e("Error", "Directory not created")
            return null
        }
        val imageFile = File(mediaStorageDir, "egs_${System.currentTimeMillis()}.jpg")
        try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            return Uri.fromFile(imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    // Function to draw text overlay on the bitmap
    fun drawTextOverlay(bitmap: Bitmap, text: String): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.RED
            textSize = 50f
            isAntiAlias = true
        }
        canvas.drawText(text, 100f, 100f, paint)
        return bitmap
    }
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
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
}