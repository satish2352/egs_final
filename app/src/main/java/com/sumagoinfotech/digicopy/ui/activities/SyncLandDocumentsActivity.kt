package com.sumagoinfotech.digicopy.ui.activities

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentDao
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLandDocumentsBinding
import com.sumagoinfotech.digicopy.adapters.DocumentPagesAdapter
import com.sumagoinfotech.digicopy.adapters.SyncLandDocumentsAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.FileInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class SyncLandDocumentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySyncLandDocumentsBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var documentDao: DocumentDao
    private lateinit var documentList: List<Document>
    private lateinit var adapter: SyncLandDocumentsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncLandDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager=GridLayoutManager(this,3,RecyclerView.VERTICAL,false)
        binding.recyclerViewSyncLandDocuments.layoutManager=layoutManager
        documentList=ArrayList<Document>()
        adapter= SyncLandDocumentsAdapter(documentList as ArrayList<Document>)
        binding.recyclerViewSyncLandDocuments.adapter=adapter
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sync_land_documents)
        appDatabase=AppDatabase.getDatabase(this)
        documentDao=appDatabase.documentDao()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        if(item.itemId==R.id.navigation_sync){

            uploadDocuments()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun uploadDocuments() {
        val dialog=CustomProgressDialog(this@SyncLandDocumentsActivity)
        dialog.show()
        val apiService = ApiClient.create(this@SyncLandDocumentsActivity)
        CoroutineScope(Dispatchers.IO).launch {
            val documents = documentDao.getAllDocuments()
            try {
                documents.forEach { document ->
                    val filePart = createFilePart(FileInfo("document_pdf", document.documentUri))
                    val response=apiService.uploadDocument(
                        documentName = document.documentName,
                        documentId =document.documentId,
                        file = filePart!!,
                        longitude = document.longitude,
                        latitude = document.latitude,
                    )
                    if(response.isSuccessful){
                        Log.d("mytag",""+response.body()?.message)
                        Log.d("mytag",""+response.body()?.status)
                        if(response.body()?.status.equals("true")){
                            document.isSynced=true
                            documentDao.updateDocument(document)
                            Log.d("mytag","Document upload successful  "+document.id)
                            updateDocumentList()
                        }else{
                            updateDocumentList()
                            Log.d("mytag","Document upload (response.unsccessful  "+document.id)
                        }
                    }else{
                        updateDocumentList()
                        Log.d("mytag","Document upload failed  "+document.id)
                    }
                }

                dialog.dismiss()

            } catch (e: Exception) {
                updateDocumentList()
                dialog.dismiss()
                Log.d("mytag","Upload Document Online Exception "+e.message)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        updateDocumentList()
    }

    private fun updateDocumentList() {
        CoroutineScope(Dispatchers.IO).launch {
            documentList = documentDao.getAllDocuments()
            Log.d("mytag", "=>" + documentList.size)
            adapter = SyncLandDocumentsAdapter(documentList)
            withContext(Dispatchers.Main) {
                binding.recyclerViewSyncLandDocuments.adapter = adapter
                adapter.notifyDataSetChanged()
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