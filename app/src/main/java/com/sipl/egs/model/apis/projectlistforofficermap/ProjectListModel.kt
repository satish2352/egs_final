package com.sipl.egs.model.apis.projectlistforofficermap

data class ProjectListModel(
    val `data`: List<ProjectMarkerData>,
    val message: String,
    val status: String
)