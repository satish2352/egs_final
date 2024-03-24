package com.sumagoinfotech.digicopy.model.apis.labourlist

data class LaboursList(
    val aadhar_image: String,
    val date_of_birth: String,
    val district_id: String,
    val family_details: List<FamilyDetail>,
    val full_name: String,
    val gender_name: String,
    val id: Int,
    val landline_number: String,
    val latitude: String,
    val longitude: String,
    val mgnrega_card_id: String,
    val mgnrega_image: String,
    val mobile_number: String,
    val profile_image: String,
    val status_name: String,
    val taluka_id: String,
    val village_id: String,
    val voter_image: String
)