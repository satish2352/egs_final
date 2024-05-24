package com.sipl.egs.ui.gramsevak.documents

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.R
import com.sipl.egs.adapters.DocsSentForApprovalAdapter
import com.sipl.egs.databinding.ActivityViewDocumentListSentForApprovalBinding
import com.sipl.egs.interfaces.OnDownloadDocumentClickListener
import com.sipl.egs.model.apis.maindocsmodel.DocumentItem
import com.sipl.egs.model.apis.maindocsmodel.MainDocsModel
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.DownloadUtils
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.utils.XFileDownloader
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentListSentForApprovalActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener,OnDownloadDocumentClickListener {
    private lateinit var binding:ActivityViewDocumentListSentForApprovalBinding
    private  lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: DocsSentForApprovalAdapter
    private lateinit var documentList:MutableList<DocumentItem>

    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog

    private var downloadId: Long = -1
    private lateinit var downloadReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewDocumentListSentForApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sent_for_approval)
        apiService= ApiClient.create(this)
        dialog= CustomProgressDialog(this)
        documentList= ArrayList()
        adapter= DocsSentForApprovalAdapter(documentList,this)
        binding.recyclerView.adapter=adapter
        binding.recyclerView.layoutManager= LinearLayoutManager(this, RecyclerView.VERTICAL,false)

            paginationAdapter= MyPaginationAdapter(0,"0",this)
            binding.recyclerViewPageNumbers.adapter=adapter
            paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            currentPage="1"

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

            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            DownloadUtils.handleDownloadCompletion(this@DocumentListSentForApprovalActivity,id,dialog)
                        }
                    }
                }
            }
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_EXPORTED)

        //getDataFromServer()
    } catch (e: Exception) {
        Log.d("mytag","DocumentListSentForApprovalActivity : onCreate : Exception => "+e.message)
        e.printStackTrace()
    }
}

override fun onResume() {
    super.onResume()
    getDataFromServer(currentPage)
}
    private fun getDataFromServer(currentPage:String) {
    try {
        dialog.show()
        val call=apiService.getSentForApprovalDocsList(pageNumber = currentPage)
        call.enqueue(object : Callback<MainDocsModel> {
            override fun onResponse(
                call: Call<MainDocsModel>,
                response: Response<MainDocsModel>
            ) {
                dialog.dismiss()
                if(response.isSuccessful)
                {
                    if(response.body()?.status.equals("true"))
                    {
                        if(!response?.body()?.data.isNullOrEmpty())
                        {
                            documentList= (response?.body()?.data as MutableList<DocumentItem>?)!!
                            adapter= DocsSentForApprovalAdapter(documentList,this@DocumentListSentForApprovalActivity)
                            binding.recyclerView.adapter=adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@DocumentListSentForApprovalActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                            //Toast.makeText(this@DocumentListSentForApprovalActivity,resources.getString(R.string.no_records_founds),Toast.LENGTH_SHORT).show()
                        }else{
                            documentList= (response?.body()?.data as MutableList<DocumentItem>?)!!
                            adapter= DocsSentForApprovalAdapter(documentList,this@DocumentListSentForApprovalActivity)
                            binding.recyclerView.adapter=adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@DocumentListSentForApprovalActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                        }

                    }else{
                        Toast.makeText(this@DocumentListSentForApprovalActivity,resources.getString(
                            R.string.please_try_again), Toast.LENGTH_SHORT).show()
                    }
                }else{

                    Toast.makeText(this@DocumentListSentForApprovalActivity,resources.getString(R.string.please_try_again),
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@DocumentListSentForApprovalActivity,resources.getString(R.string.error_occured_during_api_call),
                    Toast.LENGTH_SHORT).show()
            }
        })
    } catch (e: Exception) {
        dialog.dismiss()
        Toast.makeText(this@DocumentListSentForApprovalActivity,resources.getString(R.string.please_try_again),
            Toast.LENGTH_SHORT).show()
        Log.d("mytag","ViewLabourSentForApprovalActivity : getDataFromServer : Exception => "+e.message)
        e.printStackTrace()
    }

}

override fun onOptionsItemSelected(item: MenuItem): Boolean {

    if(item.itemId==android.R.id.home){
        finish()
    }
    return super.onOptionsItemSelected(item)
}

    override fun onPageNumberClicked(pageNumber: Int) {
        try {
            if (isInternetAvailable) {
                currentPage="$pageNumber"
                getDataFromServer("$pageNumber")
                paginationAdapter.setSelectedPage(pageNumber)
            }else{
                Toast.makeText(this@DocumentListSentForApprovalActivity,resources.getString(R.string.internet_is_not_available_please_check),Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception",e)
            e.printStackTrace()
        }

    }
    override fun onDownloadDocumentClick(url: String, fileName: String) {

        try {
            downloadId = XFileDownloader.downloadFile(this@DocumentListSentForApprovalActivity, url, fileName)
            Log.d("mytag","$downloadId")
            dialog.show()
        } catch (e: Exception) {
            Log.d("mytag","DocumentListSentForApprovalActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }
    }
}