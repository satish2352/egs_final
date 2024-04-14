package com.sipl.egs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("document_reasons")
data class DocumentReasons(
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    val created_at: String?=null,
    val id: Int,
    val is_active: Int,
    val is_deleted: String,
    val reason_name: String,
    val updated_at: String?=null
)