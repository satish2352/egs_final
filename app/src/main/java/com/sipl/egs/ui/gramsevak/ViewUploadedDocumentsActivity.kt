package com.sipl.egs.ui.gramsevak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.R
import com.sipl.egs.databinding.ActivityViewUploadedDocumetsBinding
import com.sipl.egs.model.apis.uploadeddocs.UploadedDocsModel
import com.sipl.egs.model.apis.uploadeddocs.UploadedDocument
import com.sipl.egs.adapters.UploadedPdfListAdapter
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewUploadedDocumentsActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding:ActivityViewUploadedDocumetsBinding
    private lateinit var adapter: UploadedPdfListAdapter
    private lateinit var documentList:ArrayList<UploadedDocument>
    private lateinit var apiService: ApiService

    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding= ActivityViewUploadedDocumetsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.uploaded_documents)
            binding.recyclverView.layoutManager=GridLayoutManager(this,3,RecyclerView.VERTICAL,false)
            documentList= ArrayList()
            adapter= UploadedPdfListAdapter(documentList)
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
                    } else {
                        isInternetAvailable = false
                        noInternetDialog.showDialog()
                    }
                }) { throwable: Throwable? -> }

        } catch (e: Exception) {

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
                        adapter= UploadedPdfListAdapter(documentList)
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
        currentPage="$pageNumber"
        getPdfListFromServer(currentPage = "$pageNumber")
        paginationAdapter.setSelectedPage(pageNumber)

    }
}