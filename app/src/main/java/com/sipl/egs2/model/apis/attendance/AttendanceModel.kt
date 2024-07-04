package com.sipl.egs2.model.apis.attendance

data class AttendanceModel(
    val `data`: List<AttendanceData>,
    val message: String,
    val status: String,
    val totalRecords: Int,
    val totalPages: Int,
    val page_no_to_hilight: String,
)