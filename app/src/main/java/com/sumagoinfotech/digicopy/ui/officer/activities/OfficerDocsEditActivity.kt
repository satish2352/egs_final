package com.sumagoinfotech.digicopy.ui.officer.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import com.sumagoinfotech.digicopy.database.entity.DocumentReasons
import com.sumagoinfotech.digicopy.database.entity.Reasons
import com.sumagoinfotech.digicopy.database.entity.RegistrationStatus
import com.sumagoinfotech.digicopy.databinding.ActivityOfficerDocsEditBinding
import com.sumagoinfotech.digicopy.model.apis.getlabour.HistoryDetailsItem
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourByMgnregaId
import com.sumagoinfotech.digicopy.model.apis.labourlist.LabourListModel
import com.sumagoinfotech.digicopy.model.apis.maindocsmodel.MainDocsModel
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.utils.FileDownloader
import com.sumagoinfotech.digicopy.webservice.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date

class OfficerDocsEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOfficerDocsEditBinding
    private lateinit var dialog: CustomProgressDialog
    private lateinit var appDatabase: AppDatabase
    private lateinit var documentReasonsDao: DocumentReasonsDao
    private lateinit var registrationStatusDao: RegistrationStatusDao
    private var reasonsList=ArrayList<DocumentReasons>()
    private var registrationStatusList=ArrayList<RegistrationStatus>()
    private var historyList=ArrayList<HistoryDetailsItem>()
    private var selectedStatusId=""
    private var selectedReasonsId=""
    private var remarks=""
    private var gram_document_id=""
    val statusNames= mutableListOf<String>()
    val reasonsNames= mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOfficerDocsEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gram_document_id=intent.getStringExtra("id").toString()!!
        appDatabase=AppDatabase.getDatabase(this)
        documentReasonsDao=appDatabase.documentsReasonsDao()
        registrationStatusDao=appDatabase.registrationStatusDao()
        dialog= CustomProgressDialog(this)
        binding.recyclerViewHistory.layoutManager= LinearLayoutManager(this@OfficerDocsEditActivity,
            RecyclerView.VERTICAL,false)
        var adapter=RegistrationStatusHistoryAdapter(historyList)
        CoroutineScope(Dispatchers.IO).launch {
            reasonsList=
                documentReasonsDao.getAllReasons() as ArrayList<DocumentReasons>;
            registrationStatusList= registrationStatusDao.getAllRegistrationStatus() as ArrayList<RegistrationStatus>;
            withContext(Dispatchers.Main){
                Log.d("mytag",reasonsList.size.toString())
                Log.d("mytag",registrationStatusList.size.toString())
                binding.recyclerViewHistory.adapter=adapter

                for(reason in reasonsList){

                    reasonsNames.add(reason.reason_name)
                    Log.d("mytag",reason.reason_name)
                }
                for(status in registrationStatusList){

                    statusNames.add(status.status_name)
                    Log.d("mytag",status.status_name)
                }
                //registrationStatusNames.add("Approved")
                //registrationStatusNames.add("Not Approved")
                val reasonsAdapter = ArrayAdapter(
                    this@OfficerDocsEditActivity,
                    android.R.layout.simple_list_item_1,
                    reasonsNames
                )
                val statusAdapter = ArrayAdapter(
                    this@OfficerDocsEditActivity,
                    android.R.layout.simple_list_item_1,
                    statusNames
                )
                binding.actSelectReason.visibility= View.GONE
                binding.etRemarks.visibility= View.GONE
                binding.etRemarkslayout.visibility= View.GONE
                binding.tvReason.visibility= View.GONE

                binding.actSelectReason.setAdapter(reasonsAdapter)
                binding.actSelectDocumentStatus.setAdapter(statusAdapter)
            }
        }

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
            if(selectedReasonsId.equals("1001")){
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

    override fun onResume() {
        super.onResume()
        getDocumentDetailsFromServer(gram_document_id)
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
                sendApprovedToServer()
            }else if(selectedStatusId.equals("3")){
                sendNotApprovedToServer()
            }
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("No") { dialog, which ->

            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getDocumentDetailsFromServer(gram_document_id:String) {

        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerDocsEditActivity)
            apiService.getDocumentDetails(gram_document_id).enqueue(object :
                Callback<MainDocsModel> {
                override fun onResponse(
                    call: Call<MainDocsModel>,
                    response: Response<MainDocsModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(!response.body()?.data.isNullOrEmpty()) {
                            val list=response.body()?.data
                            Log.d("mytag",""+ Gson().toJson(response.body()));
                            binding.tvDocumentName.text=list?.get(0)?.document_name
                            binding.tvDocumentDate.text= list?.get(0)?.updated_at?.let {
                                formatDate(
                                    it
                                )
                            }
                            binding.tvDocumentType.text=list?.get(0)?.document_type_name
                            val address="${list?.get(0)?.district_name}->${list?.get(0)?.taluka_name}->${list?.get(0)?.village_name}"
                            binding.tvDocumentType.text=address
                            binding.ivViewDocument.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(Uri.parse(list?.get(0)?.document_pdf), "application/pdf")
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                try {
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        this@OfficerDocsEditActivity, "No PDF viewer application found", Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            binding.ivDownloadDocument.setOnClickListener {
                                FileDownloader.downloadFile(this@OfficerDocsEditActivity,list?.get(0)?.document_pdf,list?.get(0)?.document_name)
                            }
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
                        }else {
                            Toast.makeText(this@OfficerDocsEditActivity, "No records found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else{
                        Toast.makeText(this@OfficerDocsEditActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<MainDocsModel>, t: Throwable) {
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
    @SuppressLint("SimpleDateFormat")
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")

        return try {
            val date: Date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun sendNotApprovedToServer()
    {
        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerDocsEditActivity)
            val call=apiService.sendNotApprovedDocToServer(gram_document_id=gram_document_id, reason_doc_id = selectedReasonsId, other_remark = remarks)
            call.enqueue(object :Callback<LabourListModel>{
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(response.body()?.status.equals("true"))
                        {
                            Toast.makeText(this@OfficerDocsEditActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                            finish()
                        }else {
                            Toast.makeText(this@OfficerDocsEditActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        }
                    } else{

                        Toast.makeText(this@OfficerDocsEditActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
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
    private fun sendApprovedToServer() {
        try {
            dialog.show()
            val apiService= ApiClient.create(this@OfficerDocsEditActivity)
            val call=apiService.sendApprovedDocToServer(gram_document_id)
            call.enqueue(object :Callback<LabourListModel>{
                override fun onResponse(
                    call: Call<LabourListModel>,
                    response: Response<LabourListModel>
                ) {
                    dialog.dismiss()
                    if(response.isSuccessful){
                        if(response.body()?.status.equals("true"))
                        {
                            Toast.makeText(this@OfficerDocsEditActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                            finish()
                        }else {
                            Toast.makeText(this@OfficerDocsEditActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        }
                    } else{
                        Toast.makeText(this@OfficerDocsEditActivity, "Response unsuccessful", Toast.LENGTH_SHORT).show()
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
}