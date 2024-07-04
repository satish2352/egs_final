package com.sipl.egs2.model.apis.mastersupdate

data class AreaMaster(
    val id: Int,
    val is_active: String,
    val is_new: Int,
    val is_visible: String,
    val location_id: Int,
    val location_type: String,
    val name: String,
    val parent_id: Int
)