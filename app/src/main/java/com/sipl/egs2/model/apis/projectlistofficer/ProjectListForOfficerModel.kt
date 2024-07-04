package com.sipl.egs2.model.apis.projectlistofficer

data class ProjectListForOfficerModel(
    val `data`: List<ProjectDataForOfficer>,
    val message: String,
    val status: String
)