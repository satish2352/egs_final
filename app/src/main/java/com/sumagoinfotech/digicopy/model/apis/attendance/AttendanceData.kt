package com.sumagoinfotech.digicopy.model.apis.attendance

data class AttendanceData(
    var aadhar_image: String,
    var attendance_day: String,
    var date_of_birth: String,
    var family_details: List<Any>,
    var full_name: String,
    var id: Int,
    var landline_number: String,
    var latitude: String,
    var longitude: String,
    var mgnrega_card_id: String,
    var mgnrega_image: String,
    var mobile_number: String,
    var profile_image: String,
    var project_name: String,
    var voter_image: String,
    var project_id:String,
    var updated_at:String,
)