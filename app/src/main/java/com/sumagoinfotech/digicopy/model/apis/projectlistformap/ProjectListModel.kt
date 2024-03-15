package com.sumagoinfotech.digicopy.model.apis.projectlistformap

data class ProjectListModel(
    val `data`: List<ProjectMarkerData>,
    val message: String,
    val status: String
)