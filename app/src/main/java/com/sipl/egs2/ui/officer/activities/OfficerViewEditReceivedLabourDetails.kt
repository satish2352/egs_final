package com.sipl.egs2.ui.officer.activities

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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.gson.Gson
import com.sipl.egs2.R
import com.sipl.egs2.adapters.FamilyDetailsListOnlineAdapter
import com.sipl.egs2.adapters.RegistrationStatusHistoryAdapter
import com.sipl.egs2.database.AppDatabase
import com.sipl.egs2.database.dao.ReasonsDao
import com.sipl.egs2.database.dao.RegistrationStatusDao
import com.sipl.egs2.database.entity.Reasons
import com.sipl.egs2.database.entity.RegistrationStatus
import com.sipl.egs2.databinding.ActivityOfficerViewEditReceivedLabourDetailsBinding
import com.sipl.egs2.model.apis.getlabour.HistoryDetailsItem
import com.sipl.egs2.model.apis.getlabour.LabourByMgnregaId
import com.sipl.egs2.model.apis.labourlist.LabourListModel
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.webservice.ApiClient
import io.getstream.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerViewEditReceivedLabourDetails : AppCompatActivity() {
    private lateinit var binding:ActivityOfficerViewEditReceivedLabourDetailsBinding
    private var voterIdImage=""
    private var mgnregaIdImage=""
    private var photo=""
    private var aadharImage=""
    private lateinit var dialog: CustomProgressDialog
    private lateinit var appDatabase: AppDatabase
    private lateinit var reasonsDao: ReasonsDao
    private lateinit var registrationStatusDao: RegistrationStatusDao
    private var reasonsList=ArrayList<Reasons>()
    private var registrationStatusList=ArrayList<RegistrationStatus>()
    private var historyList=ArrayList<HistoryDetailsItem>()
    private var selectedStatusId=""
    private var selectedReasonsId=""
    private var remarks=""
    private var mgnregaCardIdOfLabour=""
    private var labour_id=""
    private var isInternetAvailable = false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding=ActivityOfficerViewEditReceivedLabourDetailsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.labour_details)
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
            appDatabase=AppDatabase.getDatabase(this)
            reasonsDao=appDatabase.reasonsDao()
            registrationStatusDao=appDatabase.registrationStatusDao()
            binding.recyclerViewHistory.layoutManager=LinearLayoutManager(this@OfficerViewEditReceivedLabourDetails,RecyclerView.VERTICAL,false)
            var adapter=RegistrationStatusHistoryAdapter(historyList)
            binding.recyclerViewHistory.adapter=adapter
            adapter.notifyDataSetChanged()
            CoroutineScope(Dispatchers.IO).launch {
                reasonsList= reasonsDao.getAllReasons() as ArrayList<Reasons>;
                registrationStatusList= registrationStatusDao.getAllRegistrationStatus() as ArrayList<RegistrationStatus>;
            }
            binding.recyclerViewFamilyDetails.layoutManager=
                LinearLayoutManager(this, RecyclerView.VERTICAL,false)
            val mgnregaCardId=intent.getStringExtra("id")
            val labourId=intent.getIntExtra("labour_id",0)
            dialog= CustomProgressDialog(this)
            getLabourDetails(mgnregaCardId!!,labourId.toString()!!)
            binding.ivAadhar.setOnClickListener {
                if(isInternetAvailable){
                    showPhotoZoomDialog(aadharImage)
                }else{
                    Toast.makeText(this@OfficerViewEditReceivedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
                }

            }
            binding.ivPhoto.setOnClickListener {
                if(isInternetAvailable){
                    showPhotoZoomDialog(photo)
                }else{
                    Toast.makeText(this@OfficerViewEditReceivedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
                }

            }
            binding.ivMnregaCard.setOnClickListener {
                if(isInternetAvailable){
                    showPhotoZoomDialog(mgnregaIdImage)
                }else{
                    Toast.makeText(this@OfficerViewEditReceivedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
                }

            }
            binding.ivVoterId.setOnClickListener {
                if(isInternetAvailable){
                    showPhotoZoomDialog(voterIdImage)
                }else{
                    Toast.makeText(this@OfficerViewEditReceivedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
                }

            }
            val registrationStatusNames= mutableListOf<String>()
            val reasonsNames= mutableListOf<String>()
            for(reason in reasonsList){
                reasonsNames.add(reason.reason_name)
            }
           for(status in registrationStatusList){
                registrationStatusNames.add(status.status_name)
            }
            val reasonsAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                reasonsNames
            )
            val statusAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                registrationStatusNames
            )
            binding.actSelectReason.visibility= View.GONE
            binding.etRemarks.visibility= View.GONE
            binding.etRemarkslayout.visibility= View.GONE
            binding.tvReason.visibility=View.GONE

            binding.actSelectReason.setAdapter(reasonsAdapter)
            binding.actSelectRegistrationStatus.setAdapter(statusAdapter)
            binding.actSelectRegistrationStatus.setOnFocusChangeListener { v, hasFocus ->
                binding.actSelectRegistrationStatus.showDropDown()
            }
            binding.actSelectRegistrationStatus.setOnClickListener {
                binding.actSelectRegistrationStatus.showDropDown()
            }
            binding.actSelectRegistrationStatus.setOnItemClickListener { parent, view, position, id ->
                selectedStatusId=registrationStatusList[position].id.toString()
                if(registrationStatusList[position].id==2){
                    //approved
                    binding.actSelectReason.visibility= View.GONE
                    binding.etRemarks.visibility= View.GONE
                    binding.etRemarkslayout.visibility= View.GONE
                    binding.tvReason.visibility=View.GONE
                }else if(registrationStatusList[position].id==3) {
                    // not approved
                    binding.tvReason.visibility=View.VISIBLE
                    binding.actSelectReason.visibility=View.VISIBLE
                }
                else if(registrationStatusList[position].id==4) {
                    // rejected
                    binding.tvReason.visibility=View.GONE
                    binding.actSelectReason.visibility=View.GONE
                    binding.etRemarks.visibility=View.GONE
                    binding.etRemarkslayout.visibility=View.GONE
                }
            }
            binding.actSelectReason.setOnFocusChangeListener { v, hasFocus ->
                binding.actSelectReason.showDropDown()
            }
            binding.actSelectReason.setOnClickListener {
                binding.actSelectReason.showDropDown()
            }
            binding.actSelectReason.setOnItemClickListener { parent, view, position, id ->
                selectedReasonsId=reasonsList[position].id.toString()
                if(reasonsList[position].id.toString().equals("1001")){
                    binding.etRemarks.visibility=View.VISIBLE
                    binding.etRemarkslayout.visibility=View.VISIBLE
                }else{
                    binding.etRemarks.visibility=View.GONE
                    binding.etRemarkslayout.visibility=View.GONE
                }
            }

            binding.btnSubmit.setOnClickListener {
                if(isInternetAvailable) {
                    if (validateFields()) {
                        showConfirmationDialog(binding.actSelectRegistrationStatus.text.toString())
                    } else {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.select_all_details),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    Toast.makeText(this@OfficerViewEditReceivedLabourDetails,resources.getString(R.string.please_check_internet_connection),Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
        }
    }
    private fun sendApprovedToServer() {
        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerViewEditReceivedLabourDetails)
            val call=apiService.sendApprovedLabourResponseToServer("2",labour_id, mgnrega_card_id = mgnregaCardIdOfLabour)
            call.enqueue(object :Callback<LabourListModel>{
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(response.body()?.status.equals("true"))
                        {
                            Toast.makeText(this@OfficerViewEditReceivedLabourDetails,
                                getString(R.string.labour_registration_approved), Toast.LENGTH_LONG).show()
                           /* val intent= Intent(this@OfficerViewEditReceivedLabourDetails,OfficerMainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)*/
                            finish()
                        }else {
                            Toast.makeText(this@OfficerViewEditReceivedLabourDetails,
                                response.body()?.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@OfficerViewEditReceivedLabourDetails,
                            response.body()?.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                override fun onFailure(call: Call<LabourListModel>, t: Throwable) {

                    dialog.dismiss()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Log.d("mytag","Exception : sendApprovedToServer "+e.message)
            e.printStackTrace()
        }
    }

    private fun sendNotApprovedToServer()
    {
        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerViewEditReceivedLabourDetails)
            val call=apiService.sendNotApprovedLabourResponseToServer(labour_id = labour_id, isApproved = "3", reason_id = selectedReasonsId, other_remark = remarks)
            call.enqueue(object :Callback<LabourListModel>{
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(response.body()?.status.equals("true"))
                        {
                            Toast.makeText(this@OfficerViewEditReceivedLabourDetails,
                                getString(R.string.labour_status_upaded), Toast.LENGTH_SHORT).show()
                            finish()
                        }else {
                            Toast.makeText(this@OfficerViewEditReceivedLabourDetails,
                                response.body()?.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    } else{

                        Toast.makeText(this@OfficerViewEditReceivedLabourDetails,
                            response.body()?.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                override fun onFailure(call: Call<LabourListModel>, t: Throwable) {

                    dialog.dismiss()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            Log.d("mytag","Exception : sendApprovedToServer "+e.message)
            e.printStackTrace()
        }

    }

    private fun validateFields(): Boolean {
        var list= mutableListOf<Boolean>()
        if(binding.actSelectRegistrationStatus.enoughToFilter()){
            list.add(true)

            binding.actSelectRegistrationStatus.error=null
        }else{
            binding.actSelectRegistrationStatus.error=resources.getString(R.string.select_status)
            list.add(false)
        }
        if(selectedStatusId.equals("3")){

            if(binding.actSelectReason.enoughToFilter()){
                list.add(true)
                binding.actSelectReason.error=null
            }else{
                binding.actSelectReason.error=resources.getString(R.string.select_reason)
                list.add(false)
            }
            if(binding.etRemarks.isVisible){

                if(!binding.etRemarks.text.isNullOrEmpty() && binding.etRemarks.text.toString().length>0){
                    list.add(true)
                    binding.etRemarks.error=null
                    remarks=binding.etRemarks.text.toString()
                }else{
                    binding.etRemarks.error=resources.getString(R.string.add_remarks)
                    remarks=""
                    list.add(false)
                }
            }
        }

        return !list.contains(false)
    }

    private fun getLabourDetails(mgnregaCardId:String,labourId:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerViewEditReceivedLabourDetails)
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
                            Log.d("mytag",""+ Gson().toJson(response.body()));
                            binding.tvFullName.text=list?.get(0)?.full_name
                            binding.tvGramsevakName.text=list?.get(0)?.gramsevak_full_name
                            binding.tvGender.text=list?.get(0)?.gender_name
                            binding.tvDistritct.text=list?.get(0)?.district_name
                            binding.tvTaluka.text=list?.get(0)?.taluka_name
                            binding.tvVillage.text=list?.get(0)?.village_name
                            binding.tvMobile.text=list?.get(0)?.mobile_number
                            binding.tvLandline.text=list?.get(0)?.landline_number
                            binding.tvMnregaId.text=list?.get(0)?.mgnrega_card_id
                            binding.tvSkill.text=list?.get(0)?.skills
                            mgnregaCardIdOfLabour=list?.get(0)?.mgnrega_card_id.toString()
                            labour_id=list?.get(0)?.id.toString()
                            binding.tvDob.text=list?.get(0)?.date_of_birth
                            photo= list?.get(0)?.profile_image.toString()
                            mgnregaIdImage= list?.get(0)?.mgnrega_image.toString()
                            aadharImage= list?.get(0)?.aadhar_image.toString()
                            voterIdImage= list?.get(0)?.voter_image.toString()
                            if(!list?.get(0)?.history_details.isNullOrEmpty())
                            {
                                historyList= list?.get(0)?.history_details as ArrayList<HistoryDetailsItem>
                                var adapter = RegistrationStatusHistoryAdapter(historyList)
                                binding.recyclerViewHistory.adapter=adapter
                                adapter.notifyDataSetChanged()

                            }else{

                                    binding.tvHIstory.visibility=View.GONE
                                    binding.recyclerViewHistory.visibility=View.GONE

                            }

                            Log.d("mytag","=>"+historyList.size);
                            Glide.with(this@OfficerViewEditReceivedLabourDetails).load(mgnregaIdImage).override(200,200).into(binding.ivMnregaCard)
                            Glide.with(this@OfficerViewEditReceivedLabourDetails).load(photo).override(200,200).into(binding.ivPhoto)
                            Glide.with(this@OfficerViewEditReceivedLabourDetails).load(aadharImage).override(200,200).into(binding.ivAadhar)
                            Glide.with(this@OfficerViewEditReceivedLabourDetails).load(voterIdImage).override(200,200).into(binding.ivVoterId)
                            val familyList=response.body()?.data?.get(0)?.family_details
                            Log.d("mytag",""+familyList?.size);
                            var adapterFamily= FamilyDetailsListOnlineAdapter(familyList)
                            binding.recyclerViewFamilyDetails.adapter=adapterFamily
                            adapterFamily.notifyDataSetChanged()
                        }else {
                            Toast.makeText(this@OfficerViewEditReceivedLabourDetails, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@OfficerViewEditReceivedLabourDetails, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@OfficerViewEditReceivedLabourDetails, "Error Ocuured during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            e.printStackTrace()
            Log.d("mytag","Exception "+e.message,e);
        }catch (t:Throwable){

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
            val dialog= Dialog(this@OfficerViewEditReceivedLabourDetails)
            dialog.setContentView(R.layout.layout_zoom_image)
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(width, height)
            dialog.show()
            val photoView=dialog.findViewById<PhotoView>(R.id.photoView)
            val ivClose=dialog.findViewById<ImageView>(R.id.ivClose)
            Glide.with(this@OfficerViewEditReceivedLabourDetails)
                .load(uri)
                .into(photoView)

            ivClose.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {
            Log.d("mytag","Exception "+e.message,e);
        }
    }

    private fun showConfirmationDialog(status :String) {
        val alertDialogBuilder = AlertDialog.Builder(this@OfficerViewEditReceivedLabourDetails)

        // Set the dialog title, message, and buttons
        alertDialogBuilder.setTitle("$status")
        alertDialogBuilder.setMessage("Please confirm the user status")

        alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
            if(selectedStatusId.equals("2")){

                sendApprovedToServer()
            }else{
                sendNotApprovedToServer()
            }
            dialog.dismiss()

        }

        alertDialogBuilder.setNegativeButton(resources.getString(R.string.no)) { dialog, which ->

            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}