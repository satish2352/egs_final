package com.sipl.egs2.model.apis.uploadeddocs

data class UploadedDocument(
    val document_name: String,
    val document_pdf: String,
    val document_type_name: String,
    val updated_at: String,
    val doc_color: String,
    val id: Int
)