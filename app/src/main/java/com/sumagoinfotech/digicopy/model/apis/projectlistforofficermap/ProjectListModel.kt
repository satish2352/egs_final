package com.sumagoinfotech.digicopy.model.apis.projectlistforofficermap

data class ProjectListModel(
    val `data`: List<ProjectMarkerData>,
    val message: String,
    val status: String
)