package com.sipl.egs2.model.apis.officermapdash

data class DashboardMapOfficerModel(
    val `data`: List<OfficerDashMapMarkers>,
    val message: String,
    val status: String
)