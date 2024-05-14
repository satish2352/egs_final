package com.sipl.egs.model.apis.projectlist

data class ProjectsFromLatLongModel(
    val `project_data`: List<ProjectDataFromLatLong>,
    val message: String,
    val status: String
)