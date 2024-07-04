package com.sipl.egs2.model.apis.maritalstatus

data class MaritalStatusModel(
    val `data`: List<MaritalStatusData>,
    val message: String,
    val status: String
)