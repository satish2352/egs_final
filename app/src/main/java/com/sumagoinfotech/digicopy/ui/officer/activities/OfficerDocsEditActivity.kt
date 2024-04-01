package com.sumagoinfotech.digicopy.ui.officer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.FamilyDetailsListOnlineAdapter
import com.sumagoinfotech.digicopy.adapters.RegistrationStatusHistoryAdapter
import com.sumagoinfotech.digicopy.database.AppDatabase
import com.sumagoinfotech.digicopy.database.dao.DocumentReasonsDao
import com.sumagoinfotech.digicopy.database.dao.ReasonsDao
import com.sumagoinfotech.digicopy.database.dao.RegistrationStatusDao
import com.sumagoinfotech.digicopy.database.entity.Reasons
import com.sumagoinfotech.digicopy.database.entity.RegistrationStatus
import com.sumagoinfotech.digicopy.databinding.ActivityOfficerDocsEditBinding
import com.sumagoinfotech.digicopy.model.apis.getlabour.HistoryDetailsItem
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfficerDocsEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOfficerDocsEditBinding
    private lateinit var dialog: CustomProgressDialog
    private lateinit var appDatabase: AppDatabase
    private lateinit var documentReasonsDao: DocumentReasonsDao
    private lateinit var registrationStatusDao: RegistrationStatusDao
    private var reasonsList=ArrayList<Reasons>()
    private var registrationStatusList=ArrayList<RegistrationStatus>()
    private var historyList=ArrayList<HistoryDetailsItem>()
    private var selectedStatusId=""
    private var selectedReasonsId=""
    private var remarks=""
    private var mgnregaCardIdOfLabour=""
    private var labour_id=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOfficerDocsEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDatabase=AppDatabase.getDatabase(this)
        documentReasonsDao=appDatabase.documentsReasonsDao()
        registrationStatusDao=appDatabase.registrationStatusDao()
        binding.recyclerViewHistory.layoutManager= LinearLayoutManager(this@OfficerDocsEditActivity,
            RecyclerView.VERTICAL,false)
        var adapter=RegistrationStatusHistoryAdapter(historyList)
        CoroutineScope(Dispatchers.IO).launch {
            reasonsList= documentReasonsDao.getAllReasons() as ArrayList<Reasons>;
            registrationStatusList= registrationStatusDao.getAllRegistrationStatus() as ArrayList<RegistrationStatus>;
        }
        binding.recyclerViewHistory.adapter=adapter
        val statusNames= mutableListOf<String>()
        val reasonsNames= mutableListOf<String>()
        for(reason in reasonsList){

            reasonsNames.add(reason.reason_name)
        }
        for(status in registrationStatusList){

            statusNames.add(status.status_name)
        }
        //registrationStatusNames.add("Approved")
        //registrationStatusNames.add("Not Approved")
        val reasonsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            reasonsNames
        )
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            statusNames
        )
        binding.actSelectReason.visibility= View.GONE
        binding.etRemarks.visibility= View.GONE
        binding.etRemarkslayout.visibility= View.GONE
        binding.tvReason.visibility= View.GONE

        binding.actSelectReason.setAdapter(reasonsAdapter)
        binding.actSelectDocumentStatus.setAdapter(statusAdapter)
        binding.actSelectDocumentStatus.setOnFocusChangeListener { v, hasFocus ->
            binding.actSelectDocumentStatus.showDropDown()
        }
        binding.actSelectDocumentStatus.setOnClickListener {
            binding.actSelectDocumentStatus.showDropDown()
        }
        binding.actSelectDocumentStatus.setOnItemClickListener { parent, view, position, id ->
            selectedStatusId=registrationStatusList[position].id.toString()
            if(registrationStatusList[position].id==2)
            {
                //approved
                binding.actSelectReason.visibility= View.GONE
                binding.etRemarks.visibility= View.GONE
                binding.etRemarkslayout.visibility= View.GONE
                binding.tvReason.visibility= View.GONE

            }else if(registrationStatusList[position].id==3) {
                // not approved
                binding.tvReason.visibility= View.VISIBLE
                binding.actSelectReason.visibility= View.VISIBLE
            }
            else if(registrationStatusList[position].id==4) {
                // rejected
                binding.tvReason.visibility= View.GONE
                binding.actSelectReason.visibility= View.GONE
                binding.etRemarks.visibility= View.GONE
                binding.etRemarkslayout.visibility= View.GONE
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
            if(reasonsNames[position].equals("Other")){
                binding.etRemarks.visibility= View.VISIBLE
                binding.etRemarkslayout.visibility= View.VISIBLE
            }else{
                binding.etRemarks.visibility= View.GONE
                binding.etRemarkslayout.visibility= View.GONE
            }
        }

        binding.btnSubmit.setOnClickListener {
            if(validateFields()){
                showConfirmationDialog(binding.actSelectDocumentStatus.text.toString())
            }else{
                Toast.makeText(this,resources.getString(R.string.select_all_details), Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun validateFields(): Boolean {
        var list= mutableListOf<Boolean>()
        if(binding.actSelectDocumentStatus.enoughToFilter()){
            list.add(true)
            binding.actSelectDocumentStatus.error=null
        }else{
            binding.actSelectDocumentStatus.error=resources.getString(R.string.select_status)
            list.add(false)
        }
        if(selectedStatusId.equals("3"))
        {
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

    private fun showConfirmationDialog(status :String) {
        val alertDialogBuilder = AlertDialog.Builder(this@OfficerDocsEditActivity)

        // Set the dialog title, message, and buttons
        alertDialogBuilder.setTitle("$status")
        alertDialogBuilder.setMessage("Please confirm your action")

        alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
            if(selectedStatusId.equals("2"))
            {
                //sendApprovedToServer()
            }else if(selectedStatusId.equals("4")){
                //sendRejectedStatusToServer()
            }else{
                //sendNotApprovedToServer()
            }
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("No") { dialog, which ->

            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getDocumentDetailsFromServer(mgnregaCardId:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerDocsEditActivity)
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
                           /* binding.tvDocumentName.text=list?.get(0)?.full_name
                            binding.tvDocumentDate.text=list?.get(0)?.gender_name
                            binding.tvDocumentType.text=list?.get(0)?.district_id
                            mgnregaCardIdOfLabour=list?.get(0)?.mgnrega_card_id.toString()
                            labour_id=list?.get(0)?.id.toString()
                            if(!list?.get(0)?.history_details.isNullOrEmpty())
                            {
                                historyList= list?.get(0)?.history_details as ArrayList<HistoryDetailsItem>
                                var adapter = RegistrationStatusHistoryAdapter(historyList)
                                binding.recyclerViewHistory.adapter=adapter
                                adapter.notifyDataSetChanged()

                            }else{

                                binding.tvHIstory.visibility=View.GONE
                                binding.recyclerViewHistory.visibility=View.GONE

                            }*/

                        }else {
                            Toast.makeText(this@OfficerDocsEditActivity, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@OfficerDocsEditActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LabourByMgnregaId>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@OfficerDocsEditActivity, "Error Occurred during api call", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
            e.printStackTrace()
        }catch (t:Throwable){

        }
    }
}