package com.sipl.egs.ui.officer.activities

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
import com.sipl.egs.adapters.OfficerDocsReceivedForApprovalAdapter
import com.sipl.egs.databinding.ActivityOfficerDocsReSubmittedListBinding
import com.sipl.egs.model.apis.maindocsmodel.DocumentItem
import com.sipl.egs.model.apis.maindocsmodel.MainDocsModel
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerDocsReSubmittedListActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener {

    private lateinit var binding: ActivityOfficerDocsReSubmittedListBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: OfficerDocsReceivedForApprovalAdapter
    private lateinit var documentList: MutableList<DocumentItem>
    private var isInternetAvailable:Boolean=false
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOfficerDocsReSubmittedListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.re_submitted_docs)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            documentList = ArrayList()
            adapter = OfficerDocsReceivedForApprovalAdapter(documentList)
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
            val call = apiService.getReSubmittedDocsListForOfficer(pageNumber = currentPage)
            call.enqueue(object : Callback<MainDocsModel> {
                override fun onResponse(
                    call: Call<MainDocsModel>,
                    response: Response<MainDocsModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body()?.status.equals("true"))
                        {
                            documentList = (response?.body()?.data as MutableList<DocumentItem>?)!!
                            adapter = OfficerDocsReceivedForApprovalAdapter(documentList)
                            binding.recyclerView.adapter = adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@OfficerDocsReSubmittedListActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                        } else {
                            Toast.makeText(
                                this@OfficerDocsReSubmittedListActivity,
                                resources.getString(R.string.please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {

                        Toast.makeText(
                            this@OfficerDocsReSubmittedListActivity,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@OfficerDocsReSubmittedListActivity,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Toast.makeText(
                this@OfficerDocsReSubmittedListActivity,
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
}