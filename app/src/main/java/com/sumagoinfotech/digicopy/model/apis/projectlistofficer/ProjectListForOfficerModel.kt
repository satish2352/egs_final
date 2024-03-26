package com.sumagoinfotech.digicopy.model.apis.projectlistofficer

data class ProjectListForOfficerModel(
    val `data`: List<ProjectDataForOfficer>,
    val message: String,
    val status: String
)