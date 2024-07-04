package com.sipl.egs2.model.apis.update

data class LabourUpdateDetails(
    val `data`: List<LabourUpdateInfo>,
    val message: String,
    val status: String
)