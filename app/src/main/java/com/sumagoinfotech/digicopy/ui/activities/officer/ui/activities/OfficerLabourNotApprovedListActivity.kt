package com.sumagoinfotech.digicopy.ui.activities.officer.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sumagoinfotech.digicopy.R
import com.sumagoinfotech.digicopy.adapters.LaboursSentForApprovalAdapter
import com.sumagoinfotech.digicopy.model.apis.labourlist.LaboursList
import com.sumagoinfotech.digicopy.utils.CustomProgressDialog
import com.sumagoinfotech.digicopy.webservice.ApiService

class OfficerLabourNotApprovedListActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var dialog: CustomProgressDialog
    private lateinit var adapter: LaboursSentForApprovalAdapter
    private lateinit var labourList: ArrayList<LaboursList>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_officer_labour_not_approved_list)
    }
}