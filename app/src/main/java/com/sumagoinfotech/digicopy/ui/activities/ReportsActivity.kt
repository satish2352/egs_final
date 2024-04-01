package com.sumagoinfotech.digicopy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.databinding.ActivityReportsBinding
import com.sumagoinfotech.digicopy.adapters.LabourListByProjectAdapter
import com.sumagoinfotech.digicopy.model.apis.reportscount.ReportsCount
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labourList:List<Labour>
    lateinit var  adapter: LabourListByProjectAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityReportsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.labours_registered_online)
            val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
            binding.recyclerViewReports.layoutManager=layoutManager
            database= AppDatabase.getDatabase(this)
            labourDao=database.labourDao()
            labourList=ArrayList<Labour>()
            //adapter= LabourListInReportsAdapter(labourList)
            /*CoroutineScope(Dispatchers.IO).launch{
                    labourList=labourDao.getAllLabour()
                    Log.d("mytag","=>"+labourList.size)
                    adapter=LabourListInReportsAdapter(labourList)
        //            binding.recyclerViewSyncLabourData.adapter=adapter
        //            adapter.notifyDataSetChanged()

                    withContext(Dispatchers.Main) {
                        // Add the fetched data to the list
                        adapter=LabourListInReportsAdapter(labourList)
                        binding.recyclerViewReports.adapter=adapter
                        adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
                    }
                }*/
            binding.cardSentForApproval.setOnClickListener {

                val intent= Intent(this,ViewLaboursListSentForApprovalActivity::class.java)
                startActivity(intent)
            }
            binding.cardSentNotApproved.setOnClickListener {

                val intent= Intent(this,LaboursListNotApproved::class.java)
                startActivity(intent)
            }

            binding.cardApproved.setOnClickListener {

                val intent= Intent(this,LaboursListApproved::class.java)
                startActivity(intent)
            }
            binding.cardRejected.setOnClickListener {

                val intent= Intent(this,LaboursListRejectedActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {

        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
          //  finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getReportsCount()
    }
    private fun getReportsCount() {

        val dialog=CustomProgressDialog(this@ReportsActivity)
        try {
            dialog.show()
            val call=ApiClient.create(this@ReportsActivity).getReportCountInGramsevakLogin();
            call.enqueue(object : Callback<ReportsCount> {
                override fun onFailure(call: Call<ReportsCount>, t: Throwable) {
                    Toast.makeText(this@ReportsActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                override fun onResponse(call: Call<ReportsCount>, response: Response<ReportsCount>) {
                    dialog.dismiss()
                    if(response.isSuccessful)
                    {
                        if(response.body()?.status.equals("true"))
                        {
                            binding.tvSentForApproval.text=response?.body()?.sent_for_approval_count.toString()
                            binding.tvNotApproved.text=response?.body()?.not_approved_count.toString()
                            binding.tvApproved.text=response?.body()?.approved_count.toString()
                            binding.tvTodayCount.text=response?.body()?.today_count.toString()
                            binding.tvYearlyCount.text=response?.body()?.current_year_count.toString()
                        }
                    }else{
                        Toast.makeText(this@ReportsActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
        }
    }

}