package com.sipl.egs.ui.gramsevak

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
import com.sipl.egs.databinding.ActivityViewLabourFromMarkerClickBinding
import com.sipl.egs.model.apis.getlabour.LabourByMgnregaId
import com.sipl.egs.adapters.FamilyDetailsListOnlineAdapter
import com.sipl.egs.model.apis.getlabour.FamilyDetail
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.MySharedPref
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import io.getstream.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewLabourFromMarkerClick : AppCompatActivity() {
    private lateinit var binding:ActivityViewLabourFromMarkerClickBinding
    private lateinit var labour:LabourByMgnregaId
    private var voterIdImage=""
    private var mgnregaIdImage=""
    private var photo=""
    private var aadharImage=""
    private lateinit var dialog: CustomProgressDialog

    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewLabourFromMarkerClickBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.labour_details)
            binding.recyclerViewFamilyDetails.layoutManager=LinearLayoutManager(this, RecyclerView.VERTICAL,false)
            val mgnregaCardId=intent.getStringExtra("id")
            val labour_id=intent.getIntExtra("labour_id",0)
            dialog= CustomProgressDialog(this)

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
                        binding.scrollView.visibility= View.VISIBLE
                    } else {
                        isInternetAvailable = false
                        noInternetDialog.showDialog()
                        binding.scrollView.visibility= View.GONE
                    }
                }) { throwable: Throwable? -> }

            val mySharedPref=MySharedPref(this)
            if(mySharedPref.getRoleId()==2){
                getLabourDetailsForOfficer(mgnregaCardId!!,labour_id.toString()!!)
            }else{
                getLabourDetails(mgnregaCardId!!)
            }
        } catch (e: Exception) {
            Log.d("mytag","ViewLabourFromMarkerClick:",e)
            e.printStackTrace()
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
                            binding.tvSkill.text=list?.get(0)?.skills
                            photo= list?.get(0)?.profile_image.toString()
                            mgnregaIdImage= list?.get(0)?.mgnrega_image.toString()
                            aadharImage= list?.get(0)?.aadhar_image.toString()
                            voterIdImage= list?.get(0)?.voter_image.toString()
                            Glide.with(this@ViewLabourFromMarkerClick).load(mgnregaIdImage).override(200,200).into(binding.ivMnregaCard)
                            Glide.with(this@ViewLabourFromMarkerClick).load(photo).override(200,200).into(binding.ivPhoto)
                            Glide.with(this@ViewLabourFromMarkerClick).load(aadharImage).override(200,200).into(binding.ivAadhar)
                            Glide.with(this@ViewLabourFromMarkerClick).load(voterIdImage).override(200,200).into(binding.ivVoterId)
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


    private fun getLabourDetailsForOfficer(mgnregaCardId:String,labour_id:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@ViewLabourFromMarkerClick)
            apiService.getLabourDetailsByIdForOfficer(mgnregaCardId,labour_id).enqueue(object :
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
                            binding.tvSkill.text=list?.get(0)?.skills
                            photo= list?.get(0)?.profile_image.toString()
                            mgnregaIdImage= list?.get(0)?.mgnrega_image.toString()
                            aadharImage= list?.get(0)?.aadhar_image.toString()
                            voterIdImage= list?.get(0)?.voter_image.toString()
                            Glide.with(this@ViewLabourFromMarkerClick).load(mgnregaIdImage).override(200,200).into(binding.ivMnregaCard)
                            Glide.with(this@ViewLabourFromMarkerClick).load(photo).override(200,200).into(binding.ivPhoto)
                            Glide.with(this@ViewLabourFromMarkerClick).load(aadharImage).override(200,200).into(binding.ivAadhar)
                            Glide.with(this@ViewLabourFromMarkerClick).load(voterIdImage).override(200,200).into(binding.ivVoterId)
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
            Log.d("mytag","ViewLabourFromMarkerClick:",e)
            e.printStackTrace()
        }
    }

}