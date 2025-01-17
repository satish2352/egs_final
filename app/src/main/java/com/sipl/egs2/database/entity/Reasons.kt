package com.sipl.egs2.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("reasons")
data class Reasons(
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: String,
    val reason_name: String,
    val updated_at: String
)

