package com.sumagoinfotech.digicopy.ui.gramsevak.documents

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.DocsNotApprovedAdapter
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentListNotApprovedBinding
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentReSubmittedBinding
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.DocumentItem
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.MainDocsModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentReSubmittedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentReSubmittedBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: DocsNotApprovedAdapter
    private lateinit var documentList: MutableList<DocumentItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentReSubmittedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.not_approved)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            documentList = ArrayList()
            adapter = DocsNotApprovedAdapter(documentList)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            //getDataFromServer()
        } catch (e: Exception) {
            Log.d("mytag","@DocumentReSubmittedActivity : onCreate : Exception => " + e.message)
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        getDataFromServer()
    }
    private fun getDataFromServer() {
        try {
            dialog.show()
            val call = apiService.getReSubmittedDocsListForGramsevak()
            call.enqueue(object : Callback<MainDocsModel> {
                override fun onResponse(
                    call: Call<MainDocsModel>,
                    response: Response<MainDocsModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body()?.status.equals("true")) {
                            if (!response?.body()?.data.isNullOrEmpty()) {
                                documentList =
                                    (response?.body()?.data as MutableList<DocumentItem>?)!!
                                adapter = DocsNotApprovedAdapter(documentList)
                                binding.recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                                //Toast.makeText(this@DocumentReSubmittedActivity,resources.getString(R.string.no_records_founds),Toast.LENGTH_SHORT).show()
                            } else {
                                documentList =
                                    (response?.body()?.data as MutableList<DocumentItem>?)!!
                                adapter = DocsNotApprovedAdapter(documentList)
                                binding.recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }

                        } else {
                            Toast.makeText(
                                this@DocumentReSubmittedActivity, resources.getString(
                                    R.string.please_try_again
                                ), Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {

                        Toast.makeText(
                            this@DocumentReSubmittedActivity,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@DocumentReSubmittedActivity,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Toast.makeText(
                this@DocumentReSubmittedActivity, resources.getString(R.string.please_try_again),
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
