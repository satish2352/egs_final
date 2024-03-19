package com.sumagoinfotech.digicopy.model.apis.attendance

data class AttendanceData(
    val aadhar_image: String,
    val attendance_day: String,
    val date_of_birth: String,
    val family_details: List<Any>,
    val full_name: String,
    val id: Int,
    val landline_number: String,
    val latitude: String,
    val longitude: String,
    val mgnrega_card_id: String,
    val mgnrega_image: String,
    val mobile_number: String,
    val profile_image: String,
    val project_name: String,
    val voter_image: String
)