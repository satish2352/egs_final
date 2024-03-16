package com.sumagoinfotech.digicopy.model.apis.projectlistmarker

data class ProjectLabourListForMarker(
    val labour_data: List<LabourData>,
    val message: String,
    val project_data: List<ProjectData>,
    val status: String
)