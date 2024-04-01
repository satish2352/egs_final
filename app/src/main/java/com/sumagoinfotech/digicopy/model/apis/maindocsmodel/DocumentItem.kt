package com.sumagoinfotech.digicopy.model.apis.maindocsmodel

data class DocumentItem(
    val district_name: String,
    val document_name: String,
    val document_pdf: String,
    val document_type_name: String,
    val id: Int,
    val status_name: String,
    val taluka_name: String,
    val updated_at: String,
    val user_district: String,
    val user_taluka: String,
    val user_village: String,
    val village_name: String
)