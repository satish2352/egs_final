package com.sumagoinfotech.digicopy.model.apis.getlabour

data class FamilyDetail(
    val created_at: String,
    val date_of_birth: String,
    val full_name: String,
    val gender_id: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val labour_id: Int,
    val married_status_id: String,
    val relationship_id: String,
    val updated_at: String
)