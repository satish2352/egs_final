package com.sipl.egs.ui.gramsevak.documents

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs.R
import com.sipl.egs.adapters.DocsSentForApprovalAdapter
import com.sipl.egs.databinding.ActivityDocumentReSubmittedBinding
import com.sipl.egs.model.apis.maindocsmodel.DocumentItem
import com.sipl.egs.model.apis.maindocsmodel.MainDocsModel
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentReSubmittedActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding: ActivityDocumentReSubmittedBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: DocsSentForApprovalAdapter
    private lateinit var documentList: MutableList<DocumentItem>

    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentReSubmittedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.re_submitted_docs)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            documentList = ArrayList()
            adapter = DocsSentForApprovalAdapter(documentList)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)

            paginationAdapter= MyPaginationAdapter(0,"0",this)
            binding.recyclerViewPageNumbers.adapter=adapter
            paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            currentPage="1"
            //getDataFromServer()
        } catch (e: Exception) {
            Log.d("mytag","@DocumentReSubmittedActivity : onCreate : Exception => " + e.message)
            e.printStackTrace()
        }
    }
    override fun onResume() {
        super.onResume()
        getDataFromServer(currentPage)
    }
    override fun onPageNumberClicked(pageNumber: Int) {
        currentPage="$pageNumber"
        getDataFromServer("$pageNumber")
        paginationAdapter.setSelectedPage(pageNumber)

    }
    private fun getDataFromServer(currentPage:String) {
        try {
            dialog.show()
            val call = apiService.getReSubmittedDocsListForGramsevak(pageNumber = currentPage)
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

                                val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@DocumentReSubmittedActivity)
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()
                                paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)

                                //Toast.makeText(this@DocumentReSubmittedActivity,resources.getString(R.string.no_records_founds),Toast.LENGTH_SHORT).show()
                            } else {
                                documentList =
                                    (response?.body()?.data as MutableList<DocumentItem>?)!!
                                adapter = DocsSentForApprovalAdapter(documentList)
                                binding.recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()

                                val pageAdapter= MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@DocumentReSubmittedActivity)
                                binding.recyclerViewPageNumbers.adapter=pageAdapter
                                pageAdapter.notifyDataSetChanged()
                                paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
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
