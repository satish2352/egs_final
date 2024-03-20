package com.sumagoinfotech.digicopy.model.apis.uploadeddocs

data class UploadedDocsModel(
    val `data`: List<UploadedDocument>,
    val message: String,
    val status: String
)