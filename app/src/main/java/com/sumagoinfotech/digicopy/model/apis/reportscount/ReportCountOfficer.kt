package com.sumagoinfotech.digicopy.model.apis.reportscount

data class ReportCountOfficer(
    val approved_count: Int,
    val approved_document_count: Int,
    val message: String,
    val not_approved_count: Int,
    val not_approved_document_count: Int,
    val sent_for_approval_count: Int,
    val sent_for_approval_document_count: Int,
    val resubmitted_document_count: Int,
    val status: String
)