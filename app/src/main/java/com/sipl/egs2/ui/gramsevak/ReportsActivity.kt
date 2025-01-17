package com.sipl.egs2.ui.gramsevak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.sipl.egs2.R
import com.sipl.egs2.database.AppDatabase
import com.sipl.egs2.database.entity.Labour
import com.sipl.egs2.database.dao.LabourDao
import com.sipl.egs2.databinding.ActivityReportsBinding
import com.sipl.egs2.adapters.LabourListByProjectAdapter
import com.sipl.egs2.model.apis.reportscount.ReportsCount
import com.sipl.egs2.ui.gramsevak.documents.DocumentListApprovedActivity
import com.sipl.egs2.ui.gramsevak.documents.DocumentListNotApprovedActivity
import com.sipl.egs2.ui.gramsevak.documents.DocumentListSentForApprovalActivity
import com.sipl.egs2.ui.gramsevak.documents.DocumentReSubmittedActivity
import com.sipl.egs2.utils.CustomProgressDialog
import com.sipl.egs2.utils.NoInternetDialog
import com.sipl.egs2.webservice.ApiClient
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
            supportActionBar?.title=resources.getString(R.string.report)
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
            binding.cardSentForApproval.setOnClickListener {
                launchActivity(ViewLaboursListSentForApprovalActivity::class.java)
            }

            binding.cardSentNotApproved.setOnClickListener {
                launchActivity(LaboursListNotApproved::class.java)
            }

            binding.cardApproved.setOnClickListener {
                launchActivity(LaboursListApproved::class.java)
            }

            binding.cardDocsSentForApproval.setOnClickListener {
                launchActivity(DocumentListSentForApprovalActivity::class.java)
            }

            binding.cardReSubmittedDocs.setOnClickListener {
                val intent= Intent(this, DocumentReSubmittedActivity::class.java)
                startActivity(intent)
            }

            binding.cardReSubmittedLabour.setOnClickListener {
                launchActivity(LabourReSubmittedActivity::class.java)
            }
            binding.cardApprovedDocumets.setOnClickListener {
                launchActivity(DocumentListApprovedActivity::class.java)
            }

            binding.cardNotApprovedDocumets.setOnClickListener {
                launchActivity(DocumentListNotApprovedActivity::class.java)
            }
            binding.cardReSubmittedDocs.setOnClickListener {
                launchActivity(DocumentReSubmittedActivity::class.java)
            }

        } catch (e: Exception) {

            Log.d("mytag","ReportActivity",e)
        }

    }
    private fun launchActivity(activityClass: Class<*>) {
        if (isInternetAvailable) {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        } else {
            noInternetDialog.showDialog()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
          //  finish()
        }
        if(item.itemId==R.id.action_refresh){

            if (isInternetAvailable) {
              getReportsCount()
            } else {
                noInternetDialog.showDialog()
            }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_refresh,menu)
        return true
    }

}