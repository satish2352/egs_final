package com.sumagoinfotech.digicopy.model.apis.projectlistmarker

data class ProjectData(
    val description: String,
    val district: String,
    val end_date: String,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val project_name: String,
    val start_date: String,
    val state: String,
    val taluka: String,
    val village: String
)