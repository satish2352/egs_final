package com.sumagoinfotech.digicopy.database.model

data class LabourWithAreaNames(
    val id: Int? = null,
    val fullName: String,
    val gender: String,
    val dob: String,
    val village: String,
    val district: String,
    val taluka: String,
    val mobile: String,
    val landline: String,
    val mgnregaId: String,
    val familyDetails: String,
    val location: String,
    val aadharImage: String,
    val mgnregaIdImage: String,
    val voterIdImage: String,
    val photo: String,
    val isSynced: Boolean,
    val skilled: Boolean,
    val skill: String,
    val villageName: String?,
    val districtName: String?,
    val talukaName: String?
)
