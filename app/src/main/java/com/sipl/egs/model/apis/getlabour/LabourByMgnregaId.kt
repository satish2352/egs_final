package com.sipl.egs.model.apis.getlabour

data class LabourByMgnregaId(
    val `data`: List<LabourInfo>,
    val message: String,
    val status: String,
    val totalRecords: Int,
    val totalPages: Int,
    val page_no_to_hilight: String,
)