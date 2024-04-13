package com.sumagoinfotech.digicopy.model.apis.labourlist

data class LabourListModel(
    val `data`: List<LaboursList>,
    val message: String,
    val status: String,
    val totalRecords: Int,
    val totalPages: Int,
    val page_no_to_hilight: String,

)