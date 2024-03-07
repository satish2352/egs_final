package com.sumagoinfotech.digicopy.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityLabourDetails2Binding
import com.sumagoinfotech.digicopy.databinding.ActivitySyncLabourDataBinding

class LabourDetailsActivity2 : AppCompatActivity() {
    private lateinit var binding:ActivityLabourDetails2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLabourDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutAdd.setOnClickListener {
            showAddFamilyDetailsDialog()
        }
    }

    private fun showAddFamilyDetailsDialog() {
        val dialog=Dialog(this@LabourDetailsActivity2)
        dialog.setContentView(R.layout.layout_dialog_add_family_details)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()
    }
}