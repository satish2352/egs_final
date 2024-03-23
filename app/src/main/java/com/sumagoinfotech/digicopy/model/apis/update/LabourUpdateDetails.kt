package com.sumagoinfotech.digicopy.model.apis.update

data class LabourUpdateDetails(
    val `data`: List<LabourUpdateInfo>,
    val message: String,
    val status: String
)