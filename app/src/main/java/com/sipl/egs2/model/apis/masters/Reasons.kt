package com.sipl.egs2.model.apis.masters

data class Reasons(
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: String,
    val reason_name: String,
    val updated_at: String
)