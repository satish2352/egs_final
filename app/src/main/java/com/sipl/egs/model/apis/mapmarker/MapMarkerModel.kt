package com.sipl.egs.model.apis.mapmarker

data class MapMarkerModel(
    val map_data: List<MapData>,
    val message: String,
    val status: String
)