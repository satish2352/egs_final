package com.sumagoinfotech.digicopy.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val fullName: String,
    val gender: String,
    val dob: String,
    val district: String,
    val taluka: String,
    val village: String,
    val mobile: String,
    val landline: String,
    val mgnregaId: String,
    val familyDetails: String,
    val location: String,
    val aadharImage: String,
    val mgnregaIdImage: String,
    val voterIdImage: String,
    val photo: String,
    val isSynced: Boolean
)