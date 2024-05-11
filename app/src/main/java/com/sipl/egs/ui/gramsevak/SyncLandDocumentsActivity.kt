package com.sipl.egs.ui.gramsevak

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.net.toFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.R
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.dao.DocumentDao
import com.sipl.egs.database.entity.Document
import com.sipl.egs.databinding.ActivitySyncLandDocumentsBinding
import com.sipl.egs.adapters.SyncLandDocumentsAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.FileInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SyncLandDocumentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySyncLandDocumentsBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var documentDao: DocumentDao
    private lateinit var documentList: List<Document>
    private lateinit var adapter: SyncLandDocumentsAdapter
    private var isInternetAvailable = false
    private lateinit var noInternetDialog: NoInternetDialog
    private lateinit var dialog:CustomProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncLandDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.recyclerViewSyncLandDocuments.layoutManager = layoutManager
        documentList = ArrayList<Document>()
        adapter = SyncLandDocumentsAdapter(documentList as ArrayList<Document>)
        binding.recyclerViewSyncLandDocuments.adapter = adapter
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.sync_documents)
        appDatabase = AppDatabase.getDatabase(this)
        documentDao = appDatabase.documentDao()
        dialog=CustomProgressDialog(this)
        noInternetDialog = NoInternetDialog(this)
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

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        if (item.itemId == R.id.navigation_sync) {

            if (isInternetAvailable) {
                var count = 0;
                CoroutineScope(Dispatchers.IO).launch {
                    val countJob = async { count = documentDao.getDocumentsCount() }
                    countJob.await()

                    if (count > 0) {
                        uploadDocuments()
                    } else {
                       withContext(Dispatchers.Main){
                           Toast.makeText(
                               this@SyncLandDocumentsActivity,
                               resources.getString(R.string.no_records_found),
                               Toast.LENGTH_LONG
                           ).show()
                       }

                    }
                }

            } else {
                noInternetDialog.showDialog()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun uploadDocuments() {

        runOnUiThread {

            dialog.show()

        }
        CoroutineScope(Dispatchers.IO).launch {
            val documents = documentDao.getAllDocuments()
            try {
                val apiService = ApiClient.create(this@SyncLandDocumentsActivity)
                documents.forEach { document ->
                    val filePart = createFilePart(FileInfo("document_pdf", document.documentUri))
                    val response = apiService.uploadDocument(
                        documentName = document.documentName,
                        documentId = document.documentId,
                        file = filePart!!,
                        longitude = document.longitude,
                        latitude = document.latitude,
                    )
                    if (response.isSuccessful) {

                        Log.d("mytag", "" + response.body()?.message)
                        Log.d("mytag", "" + response.body()?.status)
                        if (response.body()?.status.equals("true")) {
                            document.isSynced = true
                            documentDao.updateDocument(document)
                            val filesList = mutableListOf<Uri>()
                            filesList.add(Uri.parse(document.documentUri))
                            deleteFilesFromFolder(filesList)
                            Log.d("mytag", "Document upload successful  " + document.id)
                            updateDocumentList()
                        } else {
                            updateDocumentList()
                            Toast.makeText(
                                this@SyncLandDocumentsActivity,
                                resources.getString(R.string.response_unsuccessfull),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        updateDocumentList()
                        Log.d("mytag", "Document upload failed  " + document.id)
                        Toast.makeText(
                            this@SyncLandDocumentsActivity,
                            resources.getString(R.string.response_unsuccessfull),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.dismiss()
            } catch (e: Exception) {
                updateDocumentList()
                dialog.dismiss()
                Log.d("mytag", "Upload Document Online Exception " + e.message)
                Toast.makeText(
                    this@SyncLandDocumentsActivity,
                    resources.getString(R.string.error_occured_during_api_call),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        updateDocumentList()
    }

    private fun updateDocumentList() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                documentList = documentDao.getAllDocuments()
                Log.d("mytag", "=>" + documentList.size)
                adapter = SyncLandDocumentsAdapter(documentList)
                withContext(Dispatchers.Main) {
                    binding.recyclerViewSyncLandDocuments.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {
            Log.d("mytag","ScanLandDocumentsActivity:",e)
            e.printStackTrace()
        }
    }

    private suspend fun createFilePart(fileInfo: FileInfo): MultipartBody.Part? {
        //val file: File? = uriToFile(applicationContext, fileInfo.fileUri)
        try {
            val file = fileInfo.fileUri.toFile()
            return file?.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData(fileInfo.fileName, it.name, requestFile)
            }
        } catch (e: Exception) {
            Log.d("mytag", "SyncLandDocumentsActivity::createFilePart() Exception " + e.message)
            e.printStackTrace()
            return null

        }
    }

    private fun String.toFile(): File? {
        val uri = Uri.parse(this)
        return uri.toFile()
    }

    private suspend fun deleteFilesFromFolder(urisToDelete: List<Uri>) {
        try {
            val mediaStorageDir = File(externalMediaDirs[0], "myfiles")
            val files = mediaStorageDir.listFiles()
            files?.forEach { file ->
                if (file.isFile) {
                    val fileUri = Uri.fromFile(file)
                    if (urisToDelete.contains(fileUri)) {
                        if (file.delete()) {
                            Log.d("mytag", "Deleted file: ${file.absolutePath}")
                        } else {
                            Log.d("mytag", "Failed to delete file: ${file.absolutePath}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("mytag", "Failed to delete file: ${e.message}")
        }
    }
}