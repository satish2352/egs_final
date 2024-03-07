package com.sumagoinfotech.digicopy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityReportsBinding
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLandDocumentsBinding
import com.sumagoinfotech.digicopy.ui.adapters.LabourReportsAdapter

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.recyclerViewReports.layoutManager=layoutManager
        val adapter=LabourReportsAdapter()
        binding.recyclerViewReports.adapter=adapter
    }
}