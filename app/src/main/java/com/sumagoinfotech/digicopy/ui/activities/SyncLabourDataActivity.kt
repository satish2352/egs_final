package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.model.LabourWithAreaNames
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLabourDataBinding
import com.sumagoinfotech.digicopy.ui.adapters.LabourListAdapter
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncLabourDataActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySyncLabourDataBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labourList:List<LabourWithAreaNames>
    lateinit var  adapter:LabourListAdapter
    private lateinit var dialog:CustomProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySyncLabourDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sync_labour_data)
        dialog=CustomProgressDialog(this)
        dialog.show()
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewSyncLabourData.layoutManager=layoutManager
        database= AppDatabase.getDatabase(this)
        labourDao=database.labourDao()
        labourList=ArrayList<LabourWithAreaNames>()
        adapter= LabourListAdapter(labourList)
        adapter.notifyDataSetChanged()



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch{
            labourList=labourDao.getLabourWithAreaNames()
            Log.d("mytag","=>"+labourList.size)
            val listWithName=labourDao.getLabourWithAreaNames()
            if(!listWithName.isNullOrEmpty()){

                Log.d("mytag",""+Gson().toJson(listWithName))
            }else{
                Log.d("mytag","Empty Or Nukk ")
            }
            withContext(Dispatchers.Main) {
                dialog.dismiss()
                adapter=LabourListAdapter(labourList)
                binding.recyclerViewSyncLabourData.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+labourList.size)

    }

    override fun onPostResume() {
        super.onPostResume()
    }


    override fun onRestart() {
        super.onRestart()
        dialog.show()
        CoroutineScope(Dispatchers.IO).launch{
            labourList=labourDao.getLabourWithAreaNames()
            Log.d("mytag","=>"+labourList.size)

            withContext(Dispatchers.Main) {
                dialog.dismiss()
                adapter=LabourListAdapter(labourList)
                binding.recyclerViewSyncLabourData.adapter=adapter
                adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
        Log.d("mytag",""+labourList.size)
    }
}