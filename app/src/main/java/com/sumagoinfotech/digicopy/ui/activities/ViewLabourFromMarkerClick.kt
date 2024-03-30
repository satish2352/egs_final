package com.sumagoinfotech.digicopy.ui.activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.databinding.ActivityViewLabourFromMarkerClickBinding
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.adapters.FamilyDetailsListOnlineAdapter
import com.sumagoinfotech.digicopy.model.apis.getlabour.FamilyDetail
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import io.getstream.photoview.PhotoView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ViewLabourFromMarkerClick : AppCompatActivity() {
    private lateinit var binding:ActivityViewLabourFromMarkerClickBinding
    private lateinit var labour:LabourByMgnregaId
    private var voterIdImage=""
    private var mgnregaIdImage=""
    private var photo=""
    private var aadharImage=""
    private lateinit var dialog: CustomProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewLabourFromMarkerClickBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resources.getString(R.string.labour_details)
            binding.recyclerViewFamilyDetails.layoutManager=LinearLayoutManager(this, RecyclerView.VERTICAL,false)
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

    }

    private fun getLabourDetails(mgnregaCardId:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@ViewLabourFromMarkerClick)
            apiService.getLabourDetailsById(mgnregaCardId).enqueue(object :
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
                            binding.tvDistritct.text=list?.get(0)?.district_name
                            binding.tvTaluka.text=list?.get(0)?.taluka_name
                            binding.tvVillage.text=list?.get(0)?.village_name
                            binding.tvMobile.text=list?.get(0)?.mobile_number
                            binding.tvLandline.text=list?.get(0)?.landline_number
                            binding.tvMnregaId.text=list?.get(0)?.mgnrega_card_id
                            binding.tvDob.text=list?.get(0)?.date_of_birth
                            photo= list?.get(0)?.profile_image.toString()
                            mgnregaIdImage= list?.get(0)?.mgnrega_image.toString()
                            aadharImage= list?.get(0)?.aadhar_image.toString()
                            voterIdImage= list?.get(0)?.voter_image.toString()
                            Glide.with(this@ViewLabourFromMarkerClick).load(mgnregaIdImage).into(binding.ivMnregaCard)
                            Glide.with(this@ViewLabourFromMarkerClick).load(photo).into(binding.ivPhoto)
                            Glide.with(this@ViewLabourFromMarkerClick).load(aadharImage).into(binding.ivAadhar)
                            Glide.with(this@ViewLabourFromMarkerClick).load(voterIdImage).into(binding.ivVoterId)
                            val familyList=response.body()?.data?.get(0)?.family_details
                            Log.d("mytag",""+familyList?.size);
                            var adapterFamily= FamilyDetailsListOnlineAdapter(familyList as ArrayList<FamilyDetail>?)
                            binding.recyclerViewFamilyDetails.adapter=adapterFamily
                            adapterFamily.notifyDataSetChanged()
                        }else {
                            Toast.makeText(this@ViewLabourFromMarkerClick, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@ViewLabourFromMarkerClick, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@ViewLabourFromMarkerClick, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
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
            val dialog= Dialog(this@ViewLabourFromMarkerClick)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@ViewLabourFromMarkerClick)
                .load(uri)
                .into(photoView)

            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {

        }
    }

}