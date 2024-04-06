package com.sumagoinfotech.digicopy.ui.gramsevak.documents

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.DocsSentForApprovalAdapter
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentListApprovedBinding
import com.sumagoinfotech.digicopy.databinding.ActivityViewDocumentListSentForApprovalBinding
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.DocumentItem
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.MainDocsModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentListApprovedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentListApprovedBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: DocsSentForApprovalAdapter
    private lateinit var documentList: MutableList<DocumentItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentListApprovedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.approved_document_list)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            documentList = ArrayList()
            adapter = DocsSentForApprovalAdapter(documentList)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            //getDataFromServer()
        } catch (e: Exception) {
            Log.d("mytag", "@DocumentListApprovedActivity : onCreate : Exception => " + e.message)
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
            val call = apiService.getApprovalDocsListForGramsevak()
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
                                adapter = DocsSentForApprovalAdapter(documentList)
                                binding.recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                                //Toast.makeText(this@DocumentListApprovedActivity,resources.getString(R.string.no_records_founds),Toast.LENGTH_SHORT).show()
                            } else {
                                documentList =
                                    (response?.body()?.data as MutableList<DocumentItem>?)!!
                                adapter = DocsSentForApprovalAdapter(documentList)
                                binding.recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }

                        } else {
                            Toast.makeText(
                                this@DocumentListApprovedActivity, resources.getString(
                                    R.string.please_try_again
                                ), Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {

                        Toast.makeText(
                            this@DocumentListApprovedActivity,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@DocumentListApprovedActivity,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Toast.makeText(
                this@DocumentListApprovedActivity, resources.getString(R.string.please_try_again),
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
