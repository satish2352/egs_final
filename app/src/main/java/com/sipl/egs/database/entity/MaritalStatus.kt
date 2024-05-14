package com.sipl.egs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("marital_status")
data class MaritalStatus(
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val maritalstatus: String,
    val updated_at: String
)