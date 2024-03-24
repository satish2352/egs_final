package com.sumagoinfotech.digicopy.model.apis.uploadeddocs

data class UploadedDocument(
    val document_name: String,
    val document_pdf: String,
    val documenttype: String,
    val id: Int
)