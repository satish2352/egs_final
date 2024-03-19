package com.sumagoinfotech.digicopy.model.apis.attendance

data class AttendanceModel(
    val `data`: List<AttendanceData>,
    val message: String,
    val status: String
)