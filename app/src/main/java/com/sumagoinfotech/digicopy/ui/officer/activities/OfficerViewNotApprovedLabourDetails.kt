package com.sumagoinfotech.digicopy.ui.officer.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.FamilyDetailsListOnlineAdapter
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.ReasonsDao
import com.sumagoinfotech.digicopy.database.dao.RegistrationStatusDao
import com.sumagoinfotech.digicopy.database.entity.Reasons
import com.sumagoinfotech.digicopy.database.entity.RegistrationStatus
import com.sumagoinfotech.digicopy.databinding.ActivityOfficerViewNotApprovedLabourDetailsBinding
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.labourlist.LabourListModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import io.getstream.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerViewNotApprovedLabourDetails : AppCompatActivity() {
    private lateinit var binding:ActivityOfficerViewNotApprovedLabourDetailsBinding
    private var voterIdImage=""
    private var mgnregaIdImage=""
    private var photo=""
    private var aadharImage=""
    private lateinit var dialog: CustomProgressDialog
    private var mgnregaCardIdOfLabour=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
        binding= ActivityOfficerViewNotApprovedLabourDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labour_details)
        binding.recyclerViewFamilyDetails.layoutManager=
            LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        val mgnregaCardId=intent.getStringExtra("id")
        dialog= CustomProgressDialog(this)
        getLabourDetails(mgnregaCardId!!)
        binding.ivAadhar.setOnClickListener {
            showPhotoZoomDialog(aadharImage)
        }
        binding.ivPhoto.setOnClickListener {
            showPhotoZoomDialog(photo)
        }
        binding.ivMnregaCard.setOnClickListener {
            showPhotoZoomDialog(mgnregaIdImage)
        }
        binding.ivVoterId.setOnClickListener {
            showPhotoZoomDialog(voterIdImage)
        }
    } catch (e: Exception) {

    }
}



private fun getLabourDetails(mgnregaCardId:String) {

    try {
        dialog.show()
        val apiService= ApiClient.create(this@OfficerViewNotApprovedLabourDetails)
        apiService.getLabourDetailsByIdForOfficer(mgnregaCardId).enqueue(object :
            Callback<LabourByMgnregaId> {
            override fun onResponse(
                call: Call<LabourByMgnregaId>,
                response: Response<LabourByMgnregaId>
            ) {
                dialog.dismiss()
                if(response.isSuccessful){
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val list=response.body()?.data
                        Log.d("mytag",""+ Gson().toJson(response.body()));
                        binding.tvFullName.text=list?.get(0)?.full_name
                        binding.tvGender.text=list?.get(0)?.gender_name
                        binding.tvDistritct.text=list?.get(0)?.district_id
                        binding.tvTaluka.text=list?.get(0)?.taluka_id
                        binding.tvVillage.text=list?.get(0)?.village_id
                        binding.tvMobile.text=list?.get(0)?.mobile_number
                        binding.tvLandline.text=list?.get(0)?.landline_number
                        binding.tvMnregaId.text=list?.get(0)?.mgnrega_card_id
                        mgnregaCardIdOfLabour=list?.get(0)?.mgnrega_card_id.toString()
                        binding.tvDob.text=list?.get(0)?.date_of_birth
                        photo= list?.get(0)?.profile_image.toString()
                        mgnregaIdImage= list?.get(0)?.mgnrega_image.toString()
                        aadharImage= list?.get(0)?.aadhar_image.toString()
                        voterIdImage= list?.get(0)?.voter_image.toString()
                        binding.tvSkills.text=list?.get(0)?.skills
                        if(!list?.get(0)?.other_remark.isNullOrEmpty()){
                            if(!list?.get(0)?.other_remark.equals("null")){
                                binding.tvRemarks.text=list?.get(0)?.other_remark
                            }
                        }
                        if(!list?.get(0)?.status_name.isNullOrEmpty()){
                            binding.tvRegistrationStatus.text=list?.get(0)?.status_name
                            if(list?.get(0)?.status_name.equals("Approved")){
                                binding.tvReason.visibility=View.GONE
                                binding.tvLabelReason.visibility=View.GONE
                                binding.tvRemarks.visibility=View.GONE
                                binding.tvLabelRemarks.visibility=View.GONE
                            }
                        }
                        if(!list?.get(0)?.reason_name.isNullOrEmpty()){
                            if(!list?.get(0)?.reason_name.equals("null")){
                                binding.tvReason.text=list?.get(0)?.reason_name
                            }
                        }
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(mgnregaIdImage).into(binding.ivMnregaCard)
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(photo).into(binding.ivPhoto)
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(aadharImage).into(binding.ivAadhar)
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(voterIdImage).into(binding.ivVoterId)
                        val familyList=response.body()?.data?.get(0)?.family_details
                        Log.d("mytag",""+familyList?.size);
                        var adapterFamily= FamilyDetailsListOnlineAdapter(familyList)
                        binding.recyclerViewFamilyDetails.adapter=adapterFamily
                        adapterFamily.notifyDataSetChanged()
                    }else {
                        Toast.makeText(this@OfficerViewNotApprovedLabourDetails, "No records found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else{
                    Toast.makeText(this@OfficerViewNotApprovedLabourDetails, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@OfficerViewNotApprovedLabourDetails, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
            }
        })
    } catch (e: Exception) {
        dialog.dismiss()
        e.printStackTrace()
    }
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {

    if(item.itemId==android.R.id.home){
        finish()
    }
    return super.onOptionsItemSelected(item)
}

private fun showPhotoZoomDialog(uri:String){

    try {
        val dialog= Dialog(this@OfficerViewNotApprovedLabourDetails)
        dialog.setContentView(R.layout.layout_zoom_image)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.show()
        val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
        val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
        Glide.with(this@OfficerViewNotApprovedLabourDetails)
            .load(uri)
            .into(photoView)

        ivClose.setOnClickListener {
            dialog.dismiss()
        }
    } catch (e: Exception) {

    }
}

}