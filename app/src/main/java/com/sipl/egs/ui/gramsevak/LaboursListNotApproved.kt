package com.sipl.egs.ui.gramsevak

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.R
import com.sipl.egs.adapters.LaboursNotApprovedAdapter
import com.sipl.egs.databinding.ActivityViewNotApprovedLabourBinding
import com.sipl.egs.model.apis.labourlist.LabourListModel
import com.sipl.egs.model.apis.labourlist.LaboursList
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

class LaboursListNotApproved : AppCompatActivity(), MyPaginationAdapter.OnPageNumberClickListener {
    private lateinit var binding: ActivityViewNotApprovedLabourBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: LaboursNotApprovedAdapter
    private lateinit var labourList: ArrayList<LaboursList>
    private lateinit var paginationAdapter: MyPaginationAdapter
    private var currentPage="1"
    private lateinit var paginationLayoutManager : LinearLayoutManager

    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityViewNotApprovedLabourBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = resources.getString(R.string.not_approved_list)
            apiService = ApiClient.create(this)
            dialog = CustomProgressDialog(this)
            labourList = ArrayList()
            adapter = LaboursNotApprovedAdapter(labourList)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)

            paginationAdapter= MyPaginationAdapter(0,"0",this)
            binding.recyclerViewPageNumbers.adapter=adapter
            paginationLayoutManager=LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
            binding.recyclerViewPageNumbers.layoutManager= paginationLayoutManager
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
            //getDataFromServer()
        } catch (e: Exception) {
            Log.d("mytag", " : onCreate : Exception => " + e.message)
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
            val call = apiService.getLaboursListNotApproved(startPageNumber = currentPage)
            call.enqueue(object : Callback<LabourListModel> {
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        if (response.body()?.status.equals("true")) {
                            labourList = (response?.body()?.data as ArrayList<LaboursList>?)!!
                            adapter = LaboursNotApprovedAdapter(labourList)
                            binding.recyclerView.adapter = adapter
                            adapter.notifyDataSetChanged()

                            val pageAdapter=MyPaginationAdapter(response.body()?.totalPages!!,response.body()?.page_no_to_hilight.toString(),this@LaboursListNotApproved)
                            binding.recyclerViewPageNumbers.adapter=pageAdapter
                            pageAdapter.notifyDataSetChanged()
                            paginationLayoutManager.scrollToPosition(Integer.parseInt(response.body()?.page_no_to_hilight.toString())-1)
                        } else {
                            Toast.makeText(
                                this@LaboursListNotApproved,
                                resources.getString(R.string.please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {

                        Toast.makeText(
                            this@LaboursListNotApproved,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LabourListModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@LaboursListNotApproved,
                        resources.getString(R.string.error_occured_during_api_call),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Toast.makeText(
                this@LaboursListNotApproved, resources.getString(R.string.please_try_again),
                Toast.LENGTH_SHORT
            ).show()
            Log.d("mytag", "ViewLabourNotApproved : getDataFromServer : Exception => " + e.message)
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
        if (isInternetAvailable) {
            currentPage="$pageNumber"
            getDataFromServer("$pageNumber")
            paginationAdapter.setSelectedPage(pageNumber)
        }else{
            Toast.makeText(this@LaboursListNotApproved,resources.getString(R.string.internet_is_not_available_please_check),Toast.LENGTH_SHORT).show()
        }

    }
}