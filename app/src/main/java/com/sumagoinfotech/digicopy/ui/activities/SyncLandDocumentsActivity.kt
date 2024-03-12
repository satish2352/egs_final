package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLandDocumentsBinding
import com.sumagoinfotech.digicopy.ui.adapters.SyncLandDocumentsAdapter

class SyncLandDocumentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySyncLandDocumentsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncLandDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager=GridLayoutManager(this,2,RecyclerView.VERTICAL,false)
        binding.recyclerViewSyncLandDocuments.layoutManager=layoutManager
        val adapter=SyncLandDocumentsAdapter()
        binding.recyclerViewSyncLandDocuments.adapter=adapter
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sync_land_documents)



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
}