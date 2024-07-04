package com.sipl.egs2.model.apis.labourlist

data class FamilyDetail(
    val date_of_birth: String,
    val full_name: String,
    val gender_id: String,
    val id: Int,
    val married_status_id: String,
    val relationship_id: String
)