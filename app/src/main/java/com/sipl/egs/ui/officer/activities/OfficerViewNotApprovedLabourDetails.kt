package com.sipl.egs.ui.officer.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sipl.egs.R
import com.sipl.egs.adapters.FamilyDetailsListOnlineAdapter
import com.sipl.egs.databinding.ActivityOfficerViewNotApprovedLabourDetailsBinding
import com.sipl.egs.model.apis.getlabour.LabourByMgnregaId
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import io.getstream.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private var isInternetAvailable = false
    private lateinit var noInternetDialog: NoInternetDialog
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
        val labourId=intent.getIntExtra("labour_id",0)
            Log.d("mytag","=>"+labourId);
            Log.d("mytag","=>"+mgnregaCardId);
        dialog= CustomProgressDialog(this)
        getLabourDetails(mgnregaCardId!!,labourId.toString()!!)
        binding.ivAadhar.setOnClickListener {

            if(isInternetAvailable){
                showPhotoZoomDialog(aadharImage)
            }else{
                Toast.makeText(this@OfficerViewNotApprovedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
            }

        }
        binding.ivPhoto.setOnClickListener {
            if(isInternetAvailable){
                showPhotoZoomDialog(photo)
            }else{
                Toast.makeText(this@OfficerViewNotApprovedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
            }

        }
        binding.ivMnregaCard.setOnClickListener {
            if(isInternetAvailable){
                showPhotoZoomDialog(mgnregaIdImage)
            }else{
                Toast.makeText(this@OfficerViewNotApprovedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
            }

        }
        binding.ivVoterId.setOnClickListener {

            if(isInternetAvailable){
                showPhotoZoomDialog(voterIdImage)
            }else{
                Toast.makeText(this@OfficerViewNotApprovedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
            }

        }
            noInternetDialog= NoInternetDialog(this)
            ReactiveNetwork
                .observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connectivity: Connectivity ->
                    Log.d("##", "=>" + connectivity.state())
                    if (connectivity.state().toString() == "CONNECTED") {
                        isInternetAvailable = true
                        noInternetDialog.hideDialog()
                    } else {
                        isInternetAvailable = false
                        noInternetDialog.showDialog()
                    }
                }) { throwable: Throwable? -> }
    } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
            e.printStackTrace()
    }
}



private fun getLabourDetails(mgnregaCardId:String,labourId:String) {

    try {
        dialog.show()
        val apiService= ApiClient.create(this@OfficerViewNotApprovedLabourDetails)
        apiService.getLabourDetailsByIdForOfficer(mgnregaCardId,labourId).enqueue(object :
            Callback<LabourByMgnregaId> {
            override fun onResponse(
                call: Call<LabourByMgnregaId>,
                response: Response<LabourByMgnregaId>
            ) {
                dialog.dismiss()
                if(response.isSuccessful){
                    if(!response.body()?.data.isNullOrEmpty()) {
                        val list=response.body()?.data
                        binding.tvFullName.text=list?.get(0)?.full_name
                        binding.tvGender.text=list?.get(0)?.gender_name
                        binding.tvGramsevakName.text=list?.get(0)?.gramsevak_full_name
                        binding.tvDistritct.text=list?.get(0)?.district_name
                        binding.tvTaluka.text=list?.get(0)?.taluka_name
                        binding.tvVillage.text=list?.get(0)?.village_name
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
                                binding.tvRegistrationStatus.visibility=View.GONE
                                binding.tvRegistrationStatusLabel.visibility=View.GONE
                            }
                        }
                        if(!list?.get(0)?.reason_name.isNullOrEmpty()){
                            if(!list?.get(0)?.reason_name.equals("null")){
                                binding.tvReason.text=list?.get(0)?.reason_name
                            }
                        }
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(mgnregaIdImage).override(200,200).into(binding.ivMnregaCard)
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(photo).override(200,200).into(binding.ivPhoto)
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(aadharImage).override(200,200).into(binding.ivAadhar)
                        Glide.with(this@OfficerViewNotApprovedLabourDetails).load(voterIdImage).override(200,200).into(binding.ivVoterId)
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
        Log.d("mytag","Exception "+e.message);
        e.printStackTrace()
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
        Log.d("mytag","Exception "+e.message);
        e.printStackTrace()
    }
}

}