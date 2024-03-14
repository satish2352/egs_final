package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentDao
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLandDocumentsBinding
import com.sumagoinfotech.digicopy.ui.adapters.DocumentPagesAdapter
import com.sumagoinfotech.digicopy.ui.adapters.SyncLandDocumentsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncLandDocumentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySyncLandDocumentsBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var documentDao: DocumentDao
    private lateinit var documentList: List<Document>
    private lateinit var adapter:SyncLandDocumentsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncLandDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager=GridLayoutManager(this,2,RecyclerView.VERTICAL,false)
        binding.recyclerViewSyncLandDocuments.layoutManager=layoutManager
        documentList=ArrayList<Document>()
        adapter=SyncLandDocumentsAdapter(documentList as ArrayList<Document>)
        binding.recyclerViewSyncLandDocuments.adapter=adapter
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.sync_land_documents)
        appDatabase=AppDatabase.getDatabase(this)
        documentDao=appDatabase.documentDao()

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
        updateDocumentList()
    }

    private fun updateDocumentList() {
        CoroutineScope(Dispatchers.IO).launch {
            documentList = documentDao.getAllDocuments()
            Log.d("mytag", "=>" + documentList.size)
            adapter = SyncLandDocumentsAdapter(documentList)
            withContext(Dispatchers.Main) {
                binding.recyclerViewSyncLandDocuments.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }
}