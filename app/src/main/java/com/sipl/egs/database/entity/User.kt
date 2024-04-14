package com.sipl.egs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val email: String,
    val password: String,
    val isActive: Boolean,
    val fullName:String
)