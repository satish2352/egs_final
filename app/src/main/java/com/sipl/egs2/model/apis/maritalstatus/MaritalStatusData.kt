package com.sipl.egs2.model.apis.maritalstatus

data class MaritalStatusData(
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val maritalstatus: String,
    val updated_at: String
)