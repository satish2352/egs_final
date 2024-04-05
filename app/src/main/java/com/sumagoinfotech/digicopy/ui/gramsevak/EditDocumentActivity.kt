package com.sumagoinfotech.digicopy.ui.gramsevak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityEditDocumentBinding

class EditDocumentActivity : AppCompatActivity() {
    private lateinit var binding:ActivityEditDocumentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}