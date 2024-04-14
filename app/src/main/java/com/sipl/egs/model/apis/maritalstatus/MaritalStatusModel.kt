package com.sipl.egs.model.apis.maritalstatus

data class MaritalStatusModel(
    val `data`: List<MaritalStatusData>,
    val message: String,
    val status: String
)