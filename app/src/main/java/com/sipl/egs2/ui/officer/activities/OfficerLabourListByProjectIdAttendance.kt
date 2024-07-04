package com.sipl.egs2.ui.officer.activities

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
import com.sipl.egs2.adapters.OfficerLabourListByProjectIdOfAttendance
import com.sipl.egs2.databinding.ActivityOfficerLabourListByProjectIdAttendanceBinding
import com.sipl.egs2.model.apis.attendance.AttendanceModel
import com.sipl.egs2.pagination.MyPaginationAdapter
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.MySharedPref
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerLabourListByProjectIdAttendance : AppCompatActivity(),
    MyPaginationAdapter.OnPageNumberClickListener  {
    private lateinit var binding:ActivityOfficerLabourListByProjectIdAttendanceBinding
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager
    private var projectId=""
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var mySharedPref: MySharedPref
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOfficerLabourListByProjectIdAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labour_list)
        progressDialog= CustomProgressDialog(this)
        mySharedPref= MySharedPref(this)
        binding.recyclerViewLabourList.layoutManager=LinearLayoutManager(this,
            RecyclerView.VERTICAL,false)
        projectId= intent.getStringExtra("id").toString()

        paginationAdapter= MyPaginationAdapter(0,"0",this)
        paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
        binding.recyclerViewPageNumbers.adapter= paginationAdapter
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
    }

    override fun onResume() {
        super.onResume()
        getLabourDetails(project_id = projectId!!, currentPage = currentPage)
    }


    private fun getLabourDetails(project_id:String,currentPage:String) {
        try {
            progressDialog.show()
            val apiService= ApiClient.create(this@OfficerLabourListByProjectIdAttendance)
            apiService.getAttendanceListForOfficerById(projectId = project_id,startPageNumber=currentPage).enqueue(object :
                Callback<AttendanceModel> {
                override fun onResponse(
                    call: Call<AttendanceModel>,
                    response: Response<AttendanceModel>
                ) {
                    progressDialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.data.isNullOrEmpty()) {
                            val list=response.body()?.data
                            var adapter= OfficerLabourListByProjectIdOfAttendance(list)
                            binding.recyclerViewLabourList.adapter=adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@OfficerLabourListByProjectIdAttendance)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                        }else {
                            paginationAdapter= MyPaginationAdapter(0,"0",this@OfficerLabourListByProjectIdAttendance)
                            paginationLayoutManager=LinearLayoutManager(this@OfficerLabourListByProjectIdAttendance, RecyclerView.HORIZONTAL,false)
                            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
                            binding.recyclerViewPageNumbers.adapter=paginationAdapter
                            paginationAdapter.notifyDataSetChanged()

                            Toast.makeText(this@OfficerLabourListByProjectIdAttendance, resources.getString(R.string.no_records_found), Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@OfficerLabourListByProjectIdAttendance, resources.getString(R.string.response_unsuccessfull), Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onFailure(call: Call<AttendanceModel>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@OfficerLabourListByProjectIdAttendance, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
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
            getLabourDetails(project_id = projectId!!, currentPage = currentPage)
        paginationAdapter.setSelectedPage(pageNumber)

    }
}