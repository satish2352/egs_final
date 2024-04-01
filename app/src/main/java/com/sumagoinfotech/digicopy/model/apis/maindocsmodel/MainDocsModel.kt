package com.sumagoinfotech.digicopy.model.apis.maindocsmodel

data class MainDocsModel(
    val `data`: List<DocumentItem>,
    val message: String,
    val status: String
)