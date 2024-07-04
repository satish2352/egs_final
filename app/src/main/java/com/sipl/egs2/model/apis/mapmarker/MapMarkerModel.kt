package com.sipl.egs2.model.apis.mapmarker

data class MapMarkerModel(
    val map_data: List<MapData>,
    val message: String,
    val status: String
)