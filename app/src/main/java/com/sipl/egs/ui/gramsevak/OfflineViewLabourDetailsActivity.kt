package com.sipl.egs.ui.gramsevak

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sipl.egs.R
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.dao.LabourDao
import com.sipl.egs.database.model.LabourWithAreaNames
import com.sipl.egs.databinding.ActivityViewLabourDetailsBinding
import com.sipl.egs.model.FamilyDetails
import com.sipl.egs.ui.registration.LabourRegistrationEdit1
import com.sipl.egs.adapters.FamilyDetailsListAdapter
import com.sipl.egs.database.dao.GenderDao
import com.sipl.egs.database.dao.SkillsDao
import io.getstream.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineViewLabourDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewLabourDetailsBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    private lateinit var genderDao: GenderDao
    private lateinit var skillsDao: SkillsDao
    lateinit var labour: LabourWithAreaNames
    private var familyDetailsList=ArrayList<FamilyDetails>()
    lateinit var adapter: FamilyDetailsListAdapter
    private var myGender=""
    private var mySkill=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding= ActivityViewLabourDetailsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.labour_details)
            var labourId=intent.extras?.getString("id")
            database= AppDatabase.getDatabase(this)
            adapter= FamilyDetailsListAdapter(familyDetailsList)
            binding.recyclerViewFamilyDetails.adapter=adapter
            binding.recyclerViewFamilyDetails.layoutManager=LinearLayoutManager(this@OfflineViewLabourDetailsActivity,RecyclerView.VERTICAL,false)
            labourDao=database.labourDao()
            genderDao=database.genderDao()
            skillsDao=database.skillsDao()
            CoroutineScope(Dispatchers.IO).launch {
                labour= labourDao.getLabourWithAreaNamesById(Integer.parseInt(labourId))!!
                genderDao.getGenderById(labour.gender)
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

                val intent= Intent(this, LabourRegistrationEdit1::class.java)
                intent.putExtra("id",labour.id.toString())
                startActivity(intent)
            }
        } catch (e: Exception) {
        }

    }

    private fun initializeFields() {

        try {
            binding.tvFullName.text=labour.fullName
            binding.tvGender.text=labour.genderName
            binding.tvDistritct.text=labour.districtName
            binding.tvTaluka.text=labour.talukaName
            binding.tvVillage.text=labour.villageName
            binding.tvMobile.text=labour.mobile
            binding.tvSkills.text=labour.skillName
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
            if(labour.isSyncFailed){
                binding.layoutSyncFailed.visibility= View.VISIBLE
                binding.tvFailedReason.text=labour.syncFailedReason
            }else{
                binding.layoutSyncFailed.visibility= View.GONE
            }
        } catch (e: Exception) {
        }
    }
    private fun loadWithGlideFromUri(uri: String, imageView: ImageView) {
        Glide.with(this@OfflineViewLabourDetailsActivity)
            .load(uri)
            .override(200,200)
            .into(imageView)
    }

    private fun showPhotoZoomDialog(uri:String){

        try {
            val dialog= Dialog(this@OfflineViewLabourDetailsActivity)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@OfflineViewLabourDetailsActivity)
                .load(uri)
                .into(photoView)

            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}