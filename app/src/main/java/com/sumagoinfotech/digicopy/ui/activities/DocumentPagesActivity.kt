package com.sumagoinfotech.digicopy.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DocumentPagesActivity : AppCompatActivity(),UpdateDocumentTypeListener {
    private lateinit var binding:ActivityDocumentPagesBinding
    private lateinit var actDocumentType:AutoCompleteTextView
    private lateinit var ivAddDocument:ImageView
    private lateinit var documentName:String
    private lateinit var database: AppDatabase
    private lateinit var documentDao:DocumentDao
    private lateinit var documentTypeDao:DocumentTypeDao
    private lateinit var scannerLauncher:ActivityResultLauncher<IntentSenderRequest>
    private lateinit var scanner:GmsDocumentScanner
    private lateinit var documentList:List<Document>
    private lateinit var adapter:DocumentPagesAdapter
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
                val result =
                    GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                result?.getPages()?.let { pages ->
                    for (page in pages) {
                        val imageUri = pages.get(0).getImageUri()
                    }
                }
                result?.getPdf()?.let { pdf ->
                    val pdfUri = pdf.getUri()
                    val pageCount = pdf.getPageCount()
                    saveFile1(pdfUri,pageCount.toString())
                }
            }
        }

        requestThePermissions()

    }
    private fun requestThePermissions() {

        PermissionX.init(this@DocumentPagesActivity)
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
        var documentTypeList:List<DocumentType> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch{
            documentTypeList=documentTypeDao.getDocumentsNotAdded()
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

//        var documentlist = listOf(
//            "7/12", "Namuna 8", "Namuna 8A"
//        )
//        val documentAdapter = ArrayAdapter(
//            this, android.R.layout.simple_list_item_1, documentlist
//        )
//        actDocumentType.setAdapter(documentAdapter)
        actDocumentType.setOnFocusChangeListener { abaad, asd ->
            actDocumentType.showDropDown()
        }

        actDocumentType.setOnClickListener {
            actDocumentType.showDropDown()
        }

        ivAddDocument.setOnClickListener {

            if(validateFields())
            {
                documentName = actDocumentType.text.toString()
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
    private fun saveRecordToDatabase(pdfUri: Uri, documentName: String,pageCount:String) {

        val document = Document(
            documentName=documentName,
            pageCount = pageCount,
            documentUri = pdfUri.toString(),
            isSynced = false)
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
                        val toast= Toast.makeText(this@DocumentPagesActivity,"Document record added successfully",Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }else{
                    runOnUiThread {
                        val toast=Toast.makeText(this@DocumentPagesActivity,"Something went wrong",Toast.LENGTH_SHORT)
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
    fun saveFile1(uri: Uri?,pageCount: String) {
        try {
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.e("Error", "Directory not created")
                return
            }
            val calendar = Calendar.getInstance()
            val customFileName = convertTimeToCustomString(calendar.timeInMillis);
            val myFile = File(mediaStorageDir, "$customFileName.pdf")
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
            saveRecordToDatabase(savedFileUri,documentName,pageCount)
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

}