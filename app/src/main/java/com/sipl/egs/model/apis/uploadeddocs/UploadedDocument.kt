package com.sipl.egs.model.apis.uploadeddocs

data class UploadedDocument(
    val document_name: String,
    val document_pdf: String,
    val document_type_name: String,
    val updated_at: String,
    val id: Int
)