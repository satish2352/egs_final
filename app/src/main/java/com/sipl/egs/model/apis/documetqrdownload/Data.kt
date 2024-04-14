package com.sipl.egs.model.apis.documetqrdownload

data class Data(
    val created_at: String,
    val document_name: String,
    val document_pdf: String,
    val document_type_id: Int,
    val document_type_name: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val latitude: String,
    val longitude: String,
    val updated_at: String,
    val user_id: Int
)