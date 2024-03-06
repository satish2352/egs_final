package com.sumagoinfotech.digicopy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetailsBinding
import com.sumagoinfotech.digicopy.databinding.ActivityMainBinding

class LabourDetailsActivity : AppCompatActivity() {
    lateinit var  binding :ActivityLabourDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabourDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnNext.setOnClickListener{
            val intent= Intent(this,LabourDetailsActivity2::class.java)
            startActivity(intent)
        }
    }
}