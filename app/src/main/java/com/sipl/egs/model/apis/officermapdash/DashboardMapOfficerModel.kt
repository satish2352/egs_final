package com.sipl.egs.model.apis.officermapdash

data class DashboardMapOfficerModel(
    val `data`: List<OfficerDashMapMarkers>,
    val message: String,
    val status: String
)