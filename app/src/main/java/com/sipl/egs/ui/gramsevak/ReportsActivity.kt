package com.sipl.egs.ui.gramsevak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs.R
import com.sipl.egs.database.AppDatabase
import com.sipl.egs.database.entity.Labour
import com.sipl.egs.database.dao.LabourDao
import com.sipl.egs.databinding.ActivityReportsBinding
import com.sipl.egs.adapters.LabourListByProjectAdapter
import com.sipl.egs.model.apis.reportscount.ReportsCount
import com.sipl.egs.ui.gramsevak.documents.DocumentListApprovedActivity
import com.sipl.egs.ui.gramsevak.documents.DocumentListNotApprovedActivity
import com.sipl.egs.ui.gramsevak.documents.DocumentListSentForApprovalActivity
import com.sipl.egs.ui.gramsevak.documents.DocumentReSubmittedActivity
import com.sipl.egs.utils.CustomProgressDialog
import com.sipl.egs.utils.NoInternetDialog
import com.sipl.egs.webservice.ApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var database: AppDatabase
    private lateinit var labourDao: LabourDao
    lateinit var labourList:List<Labour>
    lateinit var  adapter: LabourListByProjectAdapter
    private var isInternetAvailable=false
    private lateinit var noInternetDialog: NoInternetDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityReportsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title=resources.getString(R.string.labours_registered_online)
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

            database= AppDatabase.getDatabase(this)
            labourDao=database.labourDao()
            labourList=ArrayList<Labour>()
            binding.cardSentForApproval.setOnClickListener {

                val intent= Intent(this, ViewLaboursListSentForApprovalActivity::class.java)
                startActivity(intent)
            }
            binding.cardSentNotApproved.setOnClickListener {

                val intent= Intent(this, LaboursListNotApproved::class.java)
                startActivity(intent)
            }

            binding.cardApproved.setOnClickListener {

                val intent= Intent(this, LaboursListApproved::class.java)
                startActivity(intent)
            }
            binding.cardDocsSentForApproval.setOnClickListener {
                val intent= Intent(this, DocumentListSentForApprovalActivity::class.java)
                startActivity(intent)
            }
            binding.cardApprovedDocumets.setOnClickListener {
                val intent= Intent(this, DocumentListApprovedActivity::class.java)
                startActivity(intent)
            }
            binding.cardNotApprovedDocumets.setOnClickListener {
                val intent= Intent(this, DocumentListNotApprovedActivity::class.java)
                startActivity(intent)
            }
            binding.cardReSubmittedLabour.setOnClickListener {
                val intent= Intent(this, LabourReSubmittedActivity::class.java)
                startActivity(intent)
            }
            binding.cardReSubmittedDocs.setOnClickListener {
                val intent= Intent(this, DocumentReSubmittedActivity::class.java)
                startActivity(intent)
            }

        } catch (e: Exception) {

        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
          //  finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getReportsCount()
    }
    private fun getReportsCount() {

        val dialog=CustomProgressDialog(this@ReportsActivity)
        try {
            dialog.show()
            val call=ApiClient.create(this@ReportsActivity).getReportCountInGramsevakLogin();
            call.enqueue(object : Callback<ReportsCount> {
                override fun onFailure(call: Call<ReportsCount>, t: Throwable) {
                    Log.d("mytag",t.message.toString())
                    t.printStackTrace()
                    Toast.makeText(this@ReportsActivity, "Error Occurred during api call 1", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                override fun onResponse(call: Call<ReportsCount>, response: Response<ReportsCount>) {
                    dialog.dismiss()
                    if(response.isSuccessful)
                    {
                        if(response.body()?.status.equals("true"))
                        {
                            binding.tvSentForApproval.text=response?.body()?.sent_for_approval_count.toString()
                            binding.tvNotApproved.text=response?.body()?.not_approved_count.toString()
                            binding.tvApprovedCount.text=response?.body()?.approved_count.toString()
                            binding.tvTodayCount.text=response?.body()?.today_count.toString()
                            binding.tvCountReSubmittedLabour.text=response?.body()?.resubmitted_labour_count.toString()
                            binding.tvYearlyCount.text=response?.body()?.current_year_count.toString()
                            binding.tvDocsSentForApproval.text=response?.body()?.sent_for_approval_document_count.toString()
                            binding.tvApprovedDocsCount.text=response?.body()?.approved_document_count.toString()
                            binding.tvNotApprovedDocsCount.text=response?.body()?.not_approved_document_count.toString()
                            binding.tvCountReSubmittedDocs.text=response?.body()?.resubmitted_document_count.toString()
                        }
                    }else{
                        Toast.makeText(this@ReportsActivity, resources.getString(R.string.response_unsuccessfull), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception) {
            dialog.dismiss()
        }
    }

}