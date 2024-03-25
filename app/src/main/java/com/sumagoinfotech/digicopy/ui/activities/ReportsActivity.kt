package com.sumagoinfotech.digicopy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.databinding.ActivityReportsBinding
import com.sumagoinfotech.digicopy.adapters.LabourListByProjectAdapter

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
}