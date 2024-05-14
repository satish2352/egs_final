package com.sipl.egs.model

import com.google.gson.Gson
import java.io.Serializable

data class FamilyDetails(
    var fullName: String,
    var dob: String,
    var relationship: String,
    var maritalStatus: String,
    var gender: String,
    var genderId: String,
    var relationId: String,
    var maritalStatusId: String
):Serializable