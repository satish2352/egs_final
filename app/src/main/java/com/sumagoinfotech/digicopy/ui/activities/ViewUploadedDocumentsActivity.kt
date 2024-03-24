package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityViewUploadedDocumetsBinding
import com.sumagoinfotech.digicopy.model.apis.uploadeddocs.UploadedDocsModel
import com.sumagoinfotech.digicopy.model.apis.uploadeddocs.UploadedDocument
import com.sumagoinfotech.digicopy.adapters.UploadedPdfListAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewUploadedDocumentsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityViewUploadedDocumetsBinding
    private lateinit var adapter: UploadedPdfListAdapter
    private lateinit var documentList:ArrayList<UploadedDocument>
    private lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding= ActivityViewUploadedDocumetsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.uploaded_documents)
            binding.recyclverView.layoutManager=GridLayoutManager(this,2,RecyclerView.VERTICAL,false)
            documentList= ArrayList()
            adapter= UploadedPdfListAdapter(documentList)
            binding.recyclverView.adapter=adapter
            apiService=ApiClient.create(this)
            getPdfListFromServer()
        } catch (e: Exception) {

        }
    }

    private fun getPdfListFromServer() {
        val dialog=CustomProgressDialog(this@ViewUploadedDocumentsActivity)
        dialog.show()
        try {
            val call=apiService.getUploadedDocumentsList()
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
                    }else{


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
}