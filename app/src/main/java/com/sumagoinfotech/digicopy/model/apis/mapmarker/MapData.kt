package com.sumagoinfotech.digicopy.model.apis.mapmarker

data class MapData(
    val id: Int,
    val latitude: String,
    val longitude: String,
    val name: String,
    val type: String,
    val document_name: String,
    val document_pdf: String,
    val taluka_name: String,
    val user_district: String,
    val user_taluka: String,
    val user_village: String,
    val mgnrega_card_id: String,
)