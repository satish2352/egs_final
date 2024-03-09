package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityDocumentPagesBinding
import com.sumagoinfotech.digicopy.ui.adapters.DocumentPagesAdapter
import com.sumagoinfotech.digicopy.ui.adapters.FamilyDetailsAdapter

class DocumentPagesActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDocumentPagesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_pages)
        binding = ActivityDocumentPagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.upload_document)
        val layoutManager= GridLayoutManager(this,2, RecyclerView.VERTICAL,false)
        binding.recyclerViewDocumentPages.layoutManager=layoutManager;
        var adapter= DocumentPagesAdapter()
        binding.recyclerViewDocumentPages.adapter=adapter
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}