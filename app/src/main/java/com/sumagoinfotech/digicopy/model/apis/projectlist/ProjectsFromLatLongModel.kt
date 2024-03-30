package com.sumagoinfotech.digicopy.model.apis.projectlist

data class ProjectsFromLatLongModel(
    val `project_data`: List<ProjectDataFromLatLong>,
    val message: String,
    val status: String
)