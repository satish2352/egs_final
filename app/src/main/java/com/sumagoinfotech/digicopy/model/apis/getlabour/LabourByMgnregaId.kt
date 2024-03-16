package com.sumagoinfotech.digicopy.model.apis.getlabour

data class LabourByMgnregaId(
    val `data`: List<LabourInfo>,
    val message: String,
    val status: String
)