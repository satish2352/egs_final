package com.sipl.egs2.model

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