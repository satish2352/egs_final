package com.sumagoinfotech.digicopy.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity("document_type_dropdown")
data class DocumentTypeDropDown(
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    val created_at: String,
    val documenttype: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val updated_at: String
)