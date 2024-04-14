package com.sipl.egs.ui.gramsevak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs.R
import com.sipl.egs.databinding.ActivityLabourListByProjectBinding
import com.sipl.egs.model.apis.getlabour.LabourByMgnregaId
import com.sipl.egs.adapters.LabourListByProjectAdapter
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LabourListByProjectActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding:ActivityLabourListByProjectBinding
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    private var projectId=""
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var mySharedPref: MySharedPref


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourListByProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labour_list)
        progressDialog= CustomProgressDialog(this)
        mySharedPref= MySharedPref(this)
        binding.recyclerViewLabourList.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        projectId= intent.getStringExtra("id").toString()

        paginationAdapter= MyPaginationAdapter(0,"0",this)
        paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
        binding.recyclerViewPageNumbers.adapter= paginationAdapter
        currentPage="1"

        if(mySharedPref.getRoleId()==2)
        {
            getLabourDetailsForOfficer(project_id = projectId!!, currentPage = currentPage)
        }else{
            getLabourDetails(project_id = projectId!!, currentPage = currentPage)
        }

    }


    private fun getLabourDetails(project_id:String,currentPage:String) {

        try {
            progressDialog.show()
            val apiService= ApiClient.create(this@LabourListByProjectActivity)
            apiService.getLaboursByProjectId(project_id = project_id,startPageNumber=currentPage).enqueue(object :
                Callback<LabourByMgnregaId> {
                override fun onResponse(
                    call: Call<LabourByMgnregaId>,
                    response: Response<LabourByMgnregaId>
                ) {
                    progressDialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.data.isNullOrEmpty()) {
                            val list=response.body()?.data
                            var adapter= LabourListByProjectAdapter(list)
                            binding.recyclerViewLabourList.adapter=adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@LabourListByProjectActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)

                        }else {

                            paginationAdapter= MyPaginationAdapter(0,"0",this@LabourListByProjectActivity)
                            paginationLayoutManager=LinearLayoutManager(this@LabourListByProjectActivity, RecyclerView.HORIZONTAL,false)
                            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
                            binding.recyclerViewPageNumbers.adapter=paginationAdapter
                            paginationAdapter.notifyDataSetChanged()

                            Toast.makeText(this@LabourListByProjectActivity, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@LabourListByProjectActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@LabourListByProjectActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag",e.message.toString())
            progressDialog.dismiss()
        }
    }

    private fun getLabourDetailsForOfficer(project_id:String,currentPage:String) {

        try {
            progressDialog.show()
            val apiService= ApiClient.create(this@LabourListByProjectActivity)
            apiService.getLaboursByProjectIdForOfficer(project_id = project_id,startPageNumber=currentPage).enqueue(object :
                Callback<LabourByMgnregaId> {
                override fun onResponse(
                    call: Call<LabourByMgnregaId>,
                    response: Response<LabourByMgnregaId>
                ) {
                    progressDialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.data.isNullOrEmpty()) {
                            val list=response.body()?.data
                            var adapter= LabourListByProjectAdapter(list)
                            binding.recyclerViewLabourList.adapter=adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@LabourListByProjectActivity)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)

                        }else {

                            paginationAdapter= MyPaginationAdapter(0,"0",this@LabourListByProjectActivity)
                            paginationLayoutManager=LinearLayoutManager(this@LabourListByProjectActivity, RecyclerView.HORIZONTAL,false)
                            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
                            binding.recyclerViewPageNumbers.adapter=paginationAdapter
                            paginationAdapter.notifyDataSetChanged()

                            Toast.makeText(this@LabourListByProjectActivity, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@LabourListByProjectActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@LabourListByProjectActivity, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.d("mytag",e.message.toString())
            progressDialog.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPageNumberClicked(pageNumber: Int) {
        currentPage="$pageNumber"
        if(mySharedPref.getRoleId()==2)
        {
            getLabourDetailsForOfficer(project_id = projectId!!, currentPage = currentPage)
        }else{
            getLabourDetails(project_id = projectId!!, currentPage = currentPage)
        }
        paginationAdapter.setSelectedPage(pageNumber)

    }
}