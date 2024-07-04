package com.sipl.egs2.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("skills")
data class Skills(
    @PrimaryKey(autoGenerate = true) var xid: Int? = null,
    var created_at: String,
    var id: Int,
    var is_active: Int,
    var skills: String,
    var updated_at: String
)