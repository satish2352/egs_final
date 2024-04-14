package com.sipl.egs.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labours")
data class Labour(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var fullName: String,
    var gender: String,
    var dob: String,
    var district: String,
    var taluka: String,
    var village: String,
    var mobile: String,
    var landline: String,
    var mgnregaId: String,
    var familyDetails: String,
    var location: String,
    var aadharImage: String,
    var mgnregaIdImage: String,
    var voterIdImage: String,
    var photo: String,
    var latitude: String,
    var longitude: String,
    var isSynced: Boolean,
    var skilled:Boolean,
    var skill:String,
    var syncFailedReason:String?="",
    var isSyncFailed:Boolean?=false
)