package com.sipl.egs2.model.apis.projectlistforofficermap

data class ProjectListModel(
    val `data`: List<ProjectMarkerData>,
    val message: String,
    val status: String
)