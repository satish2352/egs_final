package com.sipl.egs.model.apis.reportscount

data class ReportsCount(
    val approved_count: Int,
    val approved_document_count: Int,
    val current_year_count: Int,
    val message: String,
    val not_approved_count: Int,
    val not_approved_document_count: Int,
    val resubmitted_document_count: Int,
    val sent_for_approval_count: Int,
    val sent_for_approval_document_count: Int,
    val resubmitted_labour_count: Int,
    val status: String,
    val today_count: Int,
    val document_count: Int,
)