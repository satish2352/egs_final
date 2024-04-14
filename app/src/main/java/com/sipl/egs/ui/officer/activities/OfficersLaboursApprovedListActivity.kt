package com.sipl.egs.ui.officer.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sipl.egs.R
import com.sipl.egs.adapters.LabourApprovedListAdapter
import com.sipl.egs.databinding.ActivityOfficersLaboursApprovedListBinding
import com.sipl.egs.model.apis.labourlist.LabourListModel
import com.sipl.egs.model.apis.labourlist.LaboursList
import com.sipl.egs.pagination.MyPaginationAdapter
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.webservice.ApiClient
import com.sipl.egs.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficersLaboursApprovedListActivity : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding: ActivityOfficersLaboursApprovedListBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: LabourApprovedListAdapter
    private lateinit var labourList: ArrayList<LaboursList>
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
        binding= ActivityOfficersLaboursApprovedListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.approved_list)
        apiService = ApiClient.create(this)
        dialog = CustomProgressDialog(this)
        labourList = ArrayList()
        adapter = LabourApprovedListAdapter(labourList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)


            paginationAdapter= MyPaginationAdapter(0,"0",this)
            binding.recyclerViewPageNumbers.adapter=adapter
            paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
            currentPage="1"

    } catch (e: Exception) {
        Log.d(
            "mytag",
            "OfficersLaboursApprovedList : onCreate : Exception => " + e.message
        )
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
        val call = apiService.getListOfLaboursApprovedByOfficer(pageNumber = currentPage)
        call.enqueue(object : Callback<LabourListModel> {
            override fun onResponse(
                call: Call<LabourListModel>,
                response: Response<LabourListModel>
            ) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body()?.status.equals("true")) {
                        labourList = (response?.body()?.data as ArrayList<LaboursList>?)!!
                        adapter = LabourApprovedListAdapter(labourList)
                        binding.recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()


                        val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@OfficersLaboursApprovedListActivity)
                        binding.recyclerViewPageNumbers.adapter=pageAdapter
                        pageAdapter.notifyDataSetChanged()
                        paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)

                    } else {
                        Toast.makeText(
                            this@OfficersLaboursApprovedListActivity,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {

                    Toast.makeText(
                        this@OfficersLaboursApprovedListActivity,
                        resources.getString(R.string.please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LabourListModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(
                    this@OfficersLaboursApprovedListActivity,
                    resources.getString(R.string.error_occured_during_api_call),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    } catch (e: Exception) {
        dialog.dismiss()
        Toast.makeText(
            this@OfficersLaboursApprovedListActivity,
            resources.getString(R.string.please_try_again),
            Toast.LENGTH_SHORT
        ).show()
        Log.d(
            "mytag",
            "OfficersLaboursApprovedList : getDataFromServer : Exception => " + e.message
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