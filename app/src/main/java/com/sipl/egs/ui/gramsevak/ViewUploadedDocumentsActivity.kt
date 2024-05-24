package com.sipl.egs.ui.gramsevak

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.MainActivity
import com.sipl.egs.R
import com.sipl.egs.databinding.ActivityViewUploadedDocumetsBinding
import com.sipl.egs.model.apis.uploadeddocs.UploadedDocsModel
import com.sipl.egs.model.apis.uploadeddocs.UploadedDocument
import com.sipl.egs.adapters.UploadedPdfListAdapter
import com.sipl.egs.interfaces.OnDownloadDocumentClickListener
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.FileDownloader
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.utils.XFileDownloader
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ViewUploadedDocumentsActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener,OnDownloadDocumentClickListener {
    private lateinit var binding:ActivityViewUploadedDocumetsBinding
    private lateinit var adapter: UploadedPdfListAdapter
    private lateinit var documentList:ArrayList<UploadedDocument>
    private lateinit var apiService: ApiService

    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog

    private var downloadId: Long = -1
    private lateinit var downloadReceiver: BroadcastReceiver
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding= ActivityViewUploadedDocumetsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            progressDialog= CustomProgressDialog(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.uploaded_documents)
            binding.recyclverView.layoutManager=GridLayoutManager(this,3,RecyclerView.VERTICAL,false)
            documentList= ArrayList()
            adapter= UploadedPdfListAdapter(documentList,this)
            binding.recyclverView.adapter=adapter
            apiService=ApiClient.create(this)
            paginationAdapter= MyPaginationAdapter(0,"0",this)
            paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            binding.recyclerViewPageNumbers.adapter=paginationAdapter
            currentPage="1"

            getPdfListFromServer(currentPage)

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
                        binding.recyclverView.visibility= View.VISIBLE
                        binding.recyclerViewPageNumbers.visibility= View.VISIBLE
                    } else {
                        isInternetAvailable = false
                        noInternetDialog.showDialog()
                        binding.recyclverView.visibility= View.GONE
                        binding.recyclerViewPageNumbers.visibility= View.GONE
                    }
                }) { throwable: Throwable? -> }

            // Start the download


            // Define the BroadcastReceiver
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                        Log.d("mytag","onReceive : Complete")
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            handleDownloadCompletion(id)
                        }
                    }
                }
            }

            // Register the BroadcastReceiver
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_EXPORTED
            )

        } catch (e: Exception) {
            Log.d("mytag","ViewUploadedDocsActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }
    }

    private fun getPdfListFromServer(currentPage:String) {
        val dialog=CustomProgressDialog(this@ViewUploadedDocumentsActivity)
        dialog.show()
        try {
            val call=apiService.getUploadedDocumentsList(startPageNumber = currentPage)
            call.enqueue(object :Callback<UploadedDocsModel>{
                override fun onResponse(
                    call: Call<UploadedDocsModel>,
                    response: Response<UploadedDocsModel>
                ) {
                    Log.d("mytag","Exception onResponse")
                    dialog.dismiss()
                    if(response.isSuccessful){
                        dialog.dismiss()
                        documentList= response.body()?.data as ArrayList<UploadedDocument>
                        adapter= UploadedPdfListAdapter(documentList,this@ViewUploadedDocumentsActivity)
                        binding.recyclverView.adapter=adapter
                        adapter.notifyDataSetChanged()

                        val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@ViewUploadedDocumentsActivity)
                        binding.recyclerViewPageNumbers.adapter=pageAdapter
                        pageAdapter.notifyDataSetChanged()
                        paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                    }else{

                        paginationAdapter= MyPaginationAdapter(0,"0",this@ViewUploadedDocumentsActivity)
                        binding.recyclerViewPageNumbers.adapter=paginationAdapter
                        paginationAdapter.notifyDataSetChanged()
                    }

                }

                override fun onFailure(call: Call<UploadedDocsModel>, t: Throwable) {
                    dialog.dismiss()
                    Log.d("mytag","Exception onFailure" +t.message)
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Log.d("mytag","Exception getPdfListFromServer" +e.message)
            e.printStackTrace()
        }

    }

    override fun onPageNumberClicked(pageNumber: Int) {
        try {
            currentPage="$pageNumber"
            getPdfListFromServer(currentPage = "$pageNumber")
            paginationAdapter.setSelectedPage(pageNumber)
        } catch (e: Exception) {
            Log.d("mytag","ViewUploadedDocumentsActivity:",e)
            e.printStackTrace()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }
    private fun handleDownloadCompletion(downloadId: Long) {
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // File download successful
                    val uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val uri = Uri.parse(uriString)
                    val file = File(uri.path!!)
                    val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

                    // Example action: open the downloaded file
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, "application/pdf")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                        val chooserIntent = Intent.createChooser(intent, "Open PDF with")
                        startActivity(chooserIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this@ViewUploadedDocumentsActivity,resources.getString(R.string.no_app_available_to_view_pdf),Toast.LENGTH_LONG).show()
                    }catch (e:Exception){
                        Toast.makeText(this@ViewUploadedDocumentsActivity,resources.getString(R.string.error_while_opening_pdf),Toast.LENGTH_LONG).show()
                    }
                } else {
                    val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    showDownloadFailedDialog(reason)
                }
            }
            progressDialog.dismiss()
            cursor.close()
        } catch (e: Exception) {
            Log.d("mytag","ViewUploadedDocsActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }
    }
    private fun showDownloadFailedDialog(reason: Int) {
        try {
            val message = when (reason) {
                DownloadManager.ERROR_CANNOT_RESUME -> "Download cannot resume."
                DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found."
                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists."
                DownloadManager.ERROR_FILE_ERROR -> "File error."
                DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error."
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space."
                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects."
                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code."
                DownloadManager.ERROR_UNKNOWN -> "Unknown error."
                else -> "Download failed."
            }
            AlertDialog.Builder(this@ViewUploadedDocumentsActivity)
                .setTitle("Download Failed")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Log.d("mytag","ViewUploadedDocsActivity: => Exception => ${e.message}",e)
            e.printStackTrace()        }
    }


    override fun onDownloadDocumentClick(url: String,fileName:String) {
        try {
            downloadId = XFileDownloader.downloadFile(this@ViewUploadedDocumentsActivity, url, fileName)
            Log.d("mytag","$downloadId")
            progressDialog.show()
        } catch (e: Exception) {
            Log.d("mytag","ViewUploadedDocsActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }

    }
}