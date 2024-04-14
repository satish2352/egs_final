package com.sipl.egs.model.apis.masters

data class Documenttype(
    val created_at: String,
    val document_type_name: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val updated_at: String
)