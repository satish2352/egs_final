package com.sipl.egs2.ui.officer.activities

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
import com.sipl.egs2.R
import com.sipl.egs2.adapters.OfficerDocsApprovedAdapter
import com.sipl.egs2.databinding.ActivityOfficerDocsApprovedListBinding
import com.sipl.egs2.interfaces.OnDownloadDocumentClickListener
import com.sipl.egs2.model.apis.maindocsmodel.DocumentItem
import com.sipl.egs2.model.apis.maindocsmodel.MainDocsModel
import com.sipl.egs2.pagination.MyPaginationAdapter
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.DownloadUtils
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.utils.XFileDownloader
import com.sipl.egs2.webservice.ApiClient
import com.sipl.egs2.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerDocsApprovedListActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener, OnDownloadDocumentClickListener {
    private lateinit var binding: ActivityOfficerDocsApprovedListBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: OfficerDocsApprovedAdapter
    private lateinit var documentList: MutableList<DocumentItem>
    private var isInternetAvailable:Boolean=false
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    private lateinit var noInternetDialog: NoInternetDialog
    private var downloadId: Long = -1
    private lateinit var downloadReceiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficerDocsApprovedListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.approved_document_list)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            documentList = ArrayList()
            adapter = OfficerDocsApprovedAdapter(documentList,this)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(
                this,
                RecyclerView.VERTICAL,
                false
            )
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
                        Log.d("mytag","onReceive : Complete")
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            DownloadUtils.handleDownloadCompletion(this@OfficerDocsApprovedListActivity,id,dialog)
                        }
                    }
                }
            }
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                AppCompatActivity.RECEIVER_EXPORTED)
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        getDataFromServer(currentPage)
    }

    private fun getDataFromServer(currentPage:String) {
        try {
            dialog.show()
            val call = apiService.getDocsApprovedInOfficer(pageNumber = currentPage)
            call.enqueue(object : Callback<MainDocsModel> {
                override fun onResponse(
                    call: Call<MainDocsModel>,
                    response: Response<MainDocsModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body()?.status.equals("true")) {
                            documentList = (response?.body()?.data as MutableList<DocumentItem>?)!!
                            adapter = OfficerDocsApprovedAdapter(documentList,this@OfficerDocsApprovedListActivity)
                            binding.recyclerView.adapter = adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@OfficerDocsApprovedListActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                        } else {
                            Toast.makeText(
                                this@OfficerDocsApprovedListActivity,
                                response.body()?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@OfficerDocsApprovedListActivity,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@OfficerDocsApprovedListActivity,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Toast.makeText(
                this@OfficerDocsApprovedListActivity,
                resources.getString(R.string.please_try_again),
                Toast.LENGTH_SHORT
            ).show()
            Log.d(
                "mytag",
                "ViewLabourSentForApprovalActivity : getDataFromServer : Exception => " + e.message
            )
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        currentPage="$pageNumber"
        getDataFromServer("$pageNumber")
        paginationAdapter.setSelectedPage(pageNumber)

    }
    override fun onDownloadDocumentClick(url: String, fileName: String) {

        try {
            downloadId = XFileDownloader.downloadFile(this@OfficerDocsApprovedListActivity, url, fileName)
            Log.d("mytag","$downloadId")
            dialog.show()
        } catch (e: Exception) {
            Log.d("mytag","OfficerDocsApprovedListActivity: => Exception => ${e.message}",e)
            e.printStackTrace()
        }
    }
}