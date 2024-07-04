package com.sipl.egs2.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity("relation")
data class Relation(
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    val created_at: String,
    val id: Int,
    val is_active: Int,
    val relation_title: String,
    val updated_at: String
)