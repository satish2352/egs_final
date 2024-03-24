package com.sumagoinfotech.digicopy.model.apis.masters

data class RegistrationStatus(
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val status_name: String,
    val updated_at: String
)