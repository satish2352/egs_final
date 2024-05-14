package com.sipl.egs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("registration_status")
data class RegistrationStatus (
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val status_name: String,
    val updated_at: String
)