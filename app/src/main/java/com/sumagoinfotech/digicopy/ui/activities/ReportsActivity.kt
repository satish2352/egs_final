package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.entity.User
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.databinding.ActivityReportsBinding
import com.sumagoinfotech.digicopy.ui.adapters.LabourReportsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    lateinit var userList:List<User>
    lateinit var  adapter:LabourReportsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labours_registered_online)
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewReports.layoutManager=layoutManager
        database= AppDatabase.getDatabase(this)
        userDao=database.userDao()
        userList=ArrayList<User>()
        adapter= LabourReportsAdapter(userList)
        CoroutineScope(Dispatchers.IO).launch{
            userList=userDao.getAllUsers()
            Log.d("mytag","=>"+userList.size)
            adapter=LabourReportsAdapter(userList)
//            binding.recyclerViewSyncLabourData.adapter=adapter
//            adapter.notifyDataSetChanged()

            withContext(Dispatchers.Main) {
                // Add the fetched data to the list
                adapter=LabourReportsAdapter(userList)
                binding.recyclerViewReports.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}