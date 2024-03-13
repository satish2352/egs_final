package com.sumagoinfotech.digicopy.ui.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.databinding.ActivityLabourRegistrationEdit1Binding
import com.sumagoinfotech.digicopy.databinding.ActivityViewLabourDetailsBinding
import com.sumagoinfotech.digicopy.model.FamilyDetails
import com.sumagoinfotech.digicopy.ui.activities.registration.LabourRegistrationEdit1
import com.sumagoinfotech.digicopy.ui.activities.registration.RegistrationViewModel
import com.sumagoinfotech.digicopy.ui.adapters.FamilyDetailsAdapter
import com.sumagoinfotech.digicopy.ui.adapters.FamilyDetailsListAdapter
import com.sumagoinfotech.digicopy.utils.LabourInputData
import io.getstream.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewLabourDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewLabourDetailsBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labour: Labour
    private var familyDetailsList=ArrayList<FamilyDetails>()
    lateinit var adapter: FamilyDetailsListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewLabourDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labour_details)
        var labourId=intent.extras?.getString("id")
        database= AppDatabase.getDatabase(this)
        adapter= FamilyDetailsListAdapter(familyDetailsList)
        binding.recyclerViewFamilyDetails.adapter=adapter
        binding.recyclerViewFamilyDetails.layoutManager=LinearLayoutManager(this@ViewLabourDetailsActivity,RecyclerView.VERTICAL,false)
        labourDao=database.labourDao()
        CoroutineScope(Dispatchers.IO).launch {
            labour=labourDao.getLabourById(Integer.parseInt(labourId))
            runOnUiThread {
                initializeFields()
            }
        }
        binding.ivAadhar.setOnClickListener {
            showPhotoZoomDialog(labour.aadharImage)
        }
        binding.ivPhoto.setOnClickListener {
            showPhotoZoomDialog(labour.photo)
        }
        binding.ivMnregaCard.setOnClickListener {
            showPhotoZoomDialog(labour.mgnregaIdImage)
        }
        binding.ivVoterId.setOnClickListener {
            showPhotoZoomDialog(labour.voterIdImage)
        }
        binding.fabEdit.setOnClickListener {

            val intent= Intent(this,LabourRegistrationEdit1::class.java)
            intent.putExtra("id",labour.id.toString())
            startActivity(intent)
        }

    }

    private fun initializeFields() {

        binding.tvFullName.text=labour.fullName
        binding.tvGender.text=labour.gender
        binding.tvDistritct.text=labour.district
        binding.tvTaluka.text=labour.taluka
        binding.tvVillage.text=labour.village
        binding.tvMobile.text=labour.mobile
        if(labour.landline.length<1){
            binding.tvLandline.text="-"
        }
        binding.tvLandline.text=labour.landline
        binding.tvDob.text=labour.dob
        binding.tvMnregaId.text=labour.mgnregaId
        loadWithGlideFromUri(labour.aadharImage,binding.ivAadhar)
        loadWithGlideFromUri(labour.mgnregaIdImage,binding.ivMnregaCard)
        loadWithGlideFromUri(labour.voterIdImage,binding.ivVoterId)
        loadWithGlideFromUri(labour.photo,binding.ivPhoto)
        Log.d("mytag",labour.familyDetails)
        val gson= Gson()
        val familyList: ArrayList<FamilyDetails> = gson.fromJson(labour.familyDetails, object : TypeToken<ArrayList<FamilyDetails>>() {}.type)
        familyDetailsList=familyList
        adapter= FamilyDetailsListAdapter(familyDetailsList)
        binding.recyclerViewFamilyDetails.adapter=adapter
        adapter.notifyDataSetChanged()
    }
    private fun loadWithGlideFromUri(uri: String, imageView: ImageView) {
        Glide.with(this@ViewLabourDetailsActivity)
            .load(uri)
            .into(imageView)
    }

    private fun showPhotoZoomDialog(uri:String){

        val dialog= Dialog(this@ViewLabourDetailsActivity)
        dialog.setContentView(R.layout.layout_zoom_image)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()
        val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
        val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
        Glide.with(this@ViewLabourDetailsActivity)
            .load(uri)
            .into(photoView)

        ivClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}