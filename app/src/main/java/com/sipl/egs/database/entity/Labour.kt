package com.sipl.egs.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labours")
data class Labour(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo("fullName")
    var fullName: String,
    @ColumnInfo("gender")
    var gender: String,
    @ColumnInfo("dob")
    var dob: String,
    @ColumnInfo("district")
    var district: String,
    @ColumnInfo("taluka")
    var taluka: String,
    @ColumnInfo("village")
    var village: String,
    @ColumnInfo("mobile")
    var mobile: String,
    @ColumnInfo("landline")
    var landline: String,
    @ColumnInfo("mgnregaId")
    var mgnregaId: String,
    @ColumnInfo("familyDetails")
    var familyDetails: String,
    @ColumnInfo("location")
    var location: String,
    @ColumnInfo("aadharImage")
    var aadharImage: String,
    @ColumnInfo("mgnregaIdImage")
    var mgnregaIdImage: String,
    @ColumnInfo("voterIdImage")
    var voterIdImage: String,
    @ColumnInfo("photo")
    var photo: String,
    @ColumnInfo("latitude")
    var latitude: String,
    @ColumnInfo("longitude")
    var longitude: String,
    @ColumnInfo("isSynced")
    var isSynced: Boolean,
    @ColumnInfo("skilled")
    var skilled:Boolean,
    @ColumnInfo("skill")
    var skill:String,
    @ColumnInfo("syncFailedReason")
    var syncFailedReason:String?="",
    @ColumnInfo("isSyncFailed")
    var isSyncFailed:Boolean?=false
)