package com.sumagoinfotech.digicopy.ui.officer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.LaboursListRejectedByOfficerAdapter
import com.sumagoinfotech.digicopy.databinding.ActivityOfficerLaboursRejectedListBinding
import com.sumagoinfotech.digicopy.model.apis.labourlist.LabourListModel
import com.sumagoinfotech.digicopy.model.apis.labourlist.LaboursList
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import com.sumagoinfotech.digicopy.webservice.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerLaboursRejectedListActivity : AppCompatActivity() {
    private lateinit var binding:ActivityOfficerLaboursRejectedListBinding
    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: LaboursListRejectedByOfficerAdapter
    private lateinit var labourList: ArrayList<LaboursList>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityOfficerLaboursRejectedListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try{
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.rejected_list)
        apiService = ApiClient.create(this)
        dialog = CustomProgressDialog(this)
        labourList = ArrayList()
        adapter = LaboursListRejectedByOfficerAdapter(labourList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    } catch (e: Exception) {
        Log.d(
            "mytag",
            "OfficerLaboursRejectedListActivity : onCreate : Exception => " + e.message
        )
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
        val call = apiService.getListOfLaboursRejectedByOfficer()
        call.enqueue(object : Callback<LabourListModel> {
            override fun onResponse(
                call: Call<LabourListModel>,
                response: Response<LabourListModel>
            ) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body()?.status.equals("true")) {
                        labourList = (response?.body()?.data as ArrayList<LaboursList>?)!!
                        adapter = LaboursListRejectedByOfficerAdapter(labourList)
                        binding.recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@OfficerLaboursRejectedListActivity,
                            resources.getString(R.string.please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {

                    Toast.makeText(
                        this@OfficerLaboursRejectedListActivity,
                        resources.getString(R.string.please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LabourListModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(
                    this@OfficerLaboursRejectedListActivity,
                    resources.getString(R.string.error_occured_during_api_call),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    } catch (e: Exception) {
        dialog.dismiss()
        Toast.makeText(
            this@OfficerLaboursRejectedListActivity,
            resources.getString(R.string.please_try_again),
            Toast.LENGTH_SHORT
        ).show()
        Log.d(
            "mytag",
            "OfficerLaboursRejectedListActivity : getDataFromServer : Exception => " + e.message
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