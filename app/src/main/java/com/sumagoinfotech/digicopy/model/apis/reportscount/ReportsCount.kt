package com.sumagoinfotech.digicopy.model.apis.reportscount

data class ReportsCount(
    val approved_count: Int,
    val today_count: Int,
    val current_year_count: Int,
    val message: String,
    val not_approved_count: Int,
    val sent_for_approval_count: Int,
    val status: String,
    var document_count:String
)