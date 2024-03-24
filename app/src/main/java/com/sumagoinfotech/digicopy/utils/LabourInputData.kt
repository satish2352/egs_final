package com.sumagoinfotech.digicopy.utils

import androidx.room.PrimaryKey
import java.io.Serializable

data class LabourInputData(
    var fullName: String = "",
    var gender: String = "",
    var dateOfBirth: String = "",
    var district: String = "",
    var taluka: String = "",
    var village: String = "",
    var mobile: String = "",
    var landline: String = "",
    var idCard: String = "",
    var skill: String = "",
    var family: String? = null,
):Serializable