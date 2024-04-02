package com.sumagoinfotech.digicopy.ui.officer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.OfficerDocsNotApprovedAdapter
import com.sumagoinfotech.digicopy.databinding.ActivityOfficerDocsNotApprovedListBinding
import com.sumagoinfotech.digicopy.databinding.ActivityOfficerDocsReSubmittedListBinding
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.DocumentItem
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.MainDocsModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerDocsReSubmittedListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfficerDocsReSubmittedListBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: OfficerDocsNotApprovedAdapter
    private lateinit var documentList: MutableList<DocumentItem>
    private var isInternetAvailable:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOfficerDocsReSubmittedListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.not_approved)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            documentList = ArrayList()
            adapter = OfficerDocsNotApprovedAdapter(documentList)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(
                this,
                RecyclerView.VERTICAL,
                false
            )
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
        getDataFromServer()
    }
    private fun getDataFromServer() {
        try {
            dialog.show()
            val call = apiService.getReSubmittedDocsListForOfficergit
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
                            adapter = OfficerDocsNotApprovedAdapter(documentList)
                            binding.recyclerView.adapter = adapter
                            adapter.notifyDataSetChanged()
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
}