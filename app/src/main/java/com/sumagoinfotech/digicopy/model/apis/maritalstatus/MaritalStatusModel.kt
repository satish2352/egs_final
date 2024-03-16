package com.sumagoinfotech.digicopy.model.apis.maritalstatus

data class MaritalStatusModel(
    val `data`: List<MaritalStatusData>,
    val message: String,
    val status: String
)