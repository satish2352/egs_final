package com.sipl.egs2.model.apis.officermapdash

data class OfficerDashMapMarkers(
    val District: String,
    val description: String,
    val district_name: String,
    val end_date: String,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val project_name: String,
    val start_date: String,
    val taluka: String,
    val taluka_name: String,
    val village: String,
    val village_name: String
)