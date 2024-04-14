package com.sipl.egs.model.apis.uploadeddocs

data class UploadedDocsModel(
    val `data`: List<UploadedDocument>,
    val message: String,
    val status: String,
    val totalRecords: Int,
    val totalPages: Int,
    val page_no_to_hilight: String,
)