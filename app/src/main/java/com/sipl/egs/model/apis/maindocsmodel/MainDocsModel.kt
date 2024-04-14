package com.sipl.egs.model.apis.maindocsmodel

data class MainDocsModel(
    val `data`: List<DocumentItem>,
    val message: String,
    val status: String,
    val totalRecords: Int,
    val totalPages: Int,
    val page_no_to_hilight: String,
)